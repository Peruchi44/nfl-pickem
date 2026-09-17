package com.java.fernando.nflpickem.dto;

public record LeaderBoardEntry(
        String userId,
        String displayName,
        int totalPoints,
        int rank
) {}