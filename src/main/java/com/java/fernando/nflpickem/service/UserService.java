package com.java.fernando.nflpickem.service;

import com.java.fernando.nflpickem.dto.LoginRequest;
import com.java.fernando.nflpickem.dto.UserResponse;
import com.java.fernando.nflpickem.model.User;
import com.java.fernando.nflpickem.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse loginOrRegister(LoginRequest request) {
        String cleanUsername = request.username().toLowerCase().trim();
        String originalDisplayName = request.username().trim();

        User user = userRepository.findByUsername(cleanUsername)
                .orElseGet(() -> {
                    User newUser = new User(cleanUsername, originalDisplayName);
                    return userRepository.save(newUser);
                });

        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}