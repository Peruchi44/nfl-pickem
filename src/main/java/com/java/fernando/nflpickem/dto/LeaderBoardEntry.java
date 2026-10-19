package com.java.fernando.nflpickem.dto;

public record LeaderBoardEntry(
        String userId,
        String displayName,
        int totalPoints,
        int rank,
        Integer tiebreakerDiff // Pode ser Integer para aceitar null se o jogo ainda não acabou
) {}