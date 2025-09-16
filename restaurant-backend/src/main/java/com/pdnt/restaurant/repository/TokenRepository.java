package com.pdnt.restaurant.repository;
import com.pdnt.restaurant.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findAllByUserIdAndExpiredFalseAndRevokedFalse(Long userId);
    Optional<Token> findByToken(String token);
}