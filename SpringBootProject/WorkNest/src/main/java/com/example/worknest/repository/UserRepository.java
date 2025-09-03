package com.example.worknest.repository;

import com.example.worknest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);

    // ✅ Only normal users (exclude admins)
    List<User> findByRoleIgnoreCase(String role);
}
