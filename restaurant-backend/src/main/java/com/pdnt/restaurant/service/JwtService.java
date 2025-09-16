package com.pdnt.restaurant.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;


@Service
public class JwtService {
    private final Key key;
    private final long accessExpMillis;
    private final long refreshExpMillis;


    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-exp-minutes}") long accessExpMinutes,
            @Value("${app.security.jwt.refresh-exp-days}") long refreshExpDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpMillis = accessExpMinutes * 60 * 1000L;
        this.refreshExpMillis = refreshExpDays * 24 * 60 * 60 * 1000L;
    }


    public String generateAccessToken(String username, Map<String, Object> extraClaims) {
        return buildToken(username, extraClaims, accessExpMillis);
    }


    public String generateRefreshToken(String username) {
        return buildToken(username, Map.of("typ", "refresh"), refreshExpMillis);
    }


    private String buildToken(String subject, Map<String, Object> extra, long ttlMillis) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(extra)
                .setIssuedAt(Date.from(now))
                .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }


    public boolean isTokenValid(String token) {
        try {
            parseClaims(token); // will throw if invalid/expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
