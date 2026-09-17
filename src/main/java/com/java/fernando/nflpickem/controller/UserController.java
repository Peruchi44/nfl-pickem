package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.model.User;
import com.java.fernando.nflpickem.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<User> getOrCreateUser(@RequestBody User request) {
        String name = request.getDisplayName() != null && !request.getDisplayName().isBlank()
                ? request.getDisplayName().trim()
                : (request.getUsername() != null ? request.getUsername().trim() : "");

        if (name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findByUsernameIgnoreCase(name)
                .orElseGet(() -> userRepository.save(new User(name, name)));

        return ResponseEntity.ok(user);
    }

    // --- NOVO ENDPOINT DE LOGIN POR E-MAIL ---

    public record EmailLoginRequest(String email, String name) {}

    @PostMapping("/login-email")
    public ResponseEntity<?> loginOrRegisterWithEmail(@RequestBody EmailLoginRequest req) {
        if (req.email() == null || req.email().isBlank()) {
            return ResponseEntity.badRequest().body("E-mail é obrigatório.");
        }

        String cleanEmail = req.email().toLowerCase().trim();

        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseGet(() -> {
                    String displayName = (req.name() != null && !req.name().isBlank())
                            ? req.name().trim()
                            : cleanEmail.split("@")[0];
                    return userRepository.save(new User(cleanEmail, displayName));
                });

        return ResponseEntity.ok(user);
    }
}