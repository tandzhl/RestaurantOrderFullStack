package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.request.LoginRequest;
import com.pdnt.restaurant.dto.request.RegisterRequest;
import com.pdnt.restaurant.dto.response.AuthResponse;
import com.pdnt.restaurant.dto.response.UserResponse;
import com.pdnt.restaurant.entity.Token;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.entity.enums.Role;
import com.pdnt.restaurant.exceptions.ErrorCode;
import com.pdnt.restaurant.exceptions.WebException;
import com.pdnt.restaurant.mapper.UserMapper;
import com.pdnt.restaurant.repository.TokenRepository;
import com.pdnt.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        // MapStruct map request → entity
        User user = userMapper.toUser(request);

        // Encode password thủ công
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1️⃣ Kiểm tra username tồn tại trước
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new WebException(ErrorCode.USERNAME_NOT_FOUND));

        // 2️⃣ Xác thực password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new WebException(ErrorCode.PASSWORD_INCORRECT);
        } catch (AuthenticationException ex) {
            throw new WebException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // 3️⃣ Revoke token cũ và cấp mới
        revokeAllUserTokens(user.getId());
        return issueTokens(user);
    }


    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                java.util.Map.of("role", user.getRole().name())
        );
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        tokenRepository.save(Token.builder()
                .token(accessToken)
                .user(user)
                .expired(false)
                .revoked(false)
                .refresh(false)
                .build());

        tokenRepository.save(Token.builder()
                .token(refreshToken)
                .user(user)
                .expired(false)
                .revoked(false)
                .refresh(true)
                .build());

        return new AuthResponse(accessToken, refreshToken);
    }

    private void revokeAllUserTokens(Long userId) {
        var validTokens = tokenRepository.findAllByUserIdAndExpiredFalseAndRevokedFalse(userId);
        validTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        var token = tokenRepository.findByToken(refreshToken)
                .filter(t -> t.isRefresh() && !t.isExpired() && !t.isRevoked())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!jwtService.isTokenValid(refreshToken)) {
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
            throw new RuntimeException("Refresh token expired");
        }

        var username = jwtService.extractUsername(refreshToken);
        var user = userRepository.findByUsername(username).orElseThrow();

        // rotate refresh token
        token.setExpired(true);
        token.setRevoked(true);
        tokenRepository.save(token);

        String newAccess = jwtService.generateAccessToken(username, java.util.Map.of("role", user.getRole().name()));
        String newRefresh = jwtService.generateRefreshToken(username);

        tokenRepository.save(Token.builder().token(newAccess).user(user).expired(false).revoked(false).refresh(false).build());
        tokenRepository.save(Token.builder().token(newRefresh).user(user).expired(false).revoked(false).refresh(true).build());

        return new AuthResponse(newAccess, newRefresh);
    }

    @Transactional
    public void logout(String accessToken) {
        tokenRepository.findByToken(accessToken).ifPresent(t -> {
            t.setExpired(true);
            t.setRevoked(true);
            tokenRepository.save(t);
        });
    }
}

