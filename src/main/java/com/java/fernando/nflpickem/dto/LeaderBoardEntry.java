package com.java.fernando.nflpickem.dto;

import java.util.Map;

public record LeaderBoardEntry(
        String userId,
        String displayName,
        int totalPoints,
        int rank,
        Integer tiebreakerDiff,
        Map<Integer, Integer> weeklyPoints // Chave: semana (1 a 18) | Valor: acertos daquela semana
) {}