package com.java.fernando.nflpickem.dto;

import java.util.List;

public record TeamStatsDto(
        String teamName,
        int gamesPlayed,
        int wins,
        int losses,
        int ties,
        double pointsForPerGame,
        double pointsAgainstPerGame,
        List<String> recentResults
) {}