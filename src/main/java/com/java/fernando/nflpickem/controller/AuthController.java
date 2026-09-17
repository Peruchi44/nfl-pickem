package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.dto.LoginRequest;
import com.java.fernando.nflpickem.dto.UserResponse;
import com.java.fernando.nflpickem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse response = userService.loginOrRegister(request);
        return ResponseEntity.ok(response);
    }
}