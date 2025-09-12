package com.example.email_verify;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByActivationToken(String token);
    List<Users> findByActivatedTrue();
    boolean existsByEmail(String email);
    Optional<Users> findByEmail(String email);
}
