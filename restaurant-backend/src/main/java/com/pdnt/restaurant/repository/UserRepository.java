package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsById(Long id);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
}
