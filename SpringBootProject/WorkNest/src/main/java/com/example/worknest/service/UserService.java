package com.example.worknest.service;

import com.example.worknest.model.User;
import com.example.worknest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    public User create(String username, String password, String role) {
        userRepo.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Username already exists");
        });
        return userRepo.save(User.builder()
                .username(username)
                .password(password)
                .role(role.toUpperCase())
                .build());
    }

    public User update(Long id, String username, String role) {
        User u = userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!u.getUsername().equals(username)) {
            userRepo.findByUsername(username).ifPresent(ex -> {
                throw new IllegalArgumentException("Username already exists");
            });
        }
        u.setUsername(username);
        u.setRole(role.toUpperCase());
        return userRepo.save(u);
    }

    public void delete(Long id) { userRepo.deleteById(id); }

    public List<User> findAll() { return userRepo.findAll(); }

    public List<User> findAllNonAdmins() { return userRepo.findByRoleIgnoreCase("USER"); } // ✅ Only users

    public Optional<User> findById(Long id) { return userRepo.findById(id); }

    public Optional<User> findByUsername(String username) { return userRepo.findByUsername(username); }

    public Optional<User> validateLogin(String username, String password) {
        return userRepo.findByUsernameAndPassword(username, password);
    }
}
