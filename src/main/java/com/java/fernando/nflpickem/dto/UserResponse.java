package com.java.fernando.nflpickem.dto;

import com.java.fernando.nflpickem.model.User;

public record UserResponse(
        String id,
        String username,
        String displayName
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName());}}