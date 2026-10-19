package com.java.fernando.nflpickem.dto;

public record QbStatsDTO(
        String qbName,
        String team,
        String headshotUrl,
        int passingYards,
        int passingTds,
        int interceptions,
        double completionPercentage,
        double passerRating
) {}