package com.java.fernando.nflpickem.dto;

public record InjuryReportDTO(
        String playerName,
        String position,
        String team,
        String injuryDetail, // Ex: "Hamstring", "Ankle", "Concussion"
        String status,       // "OUT", "DOUBTFUL", "QUESTIONABLE", "IR"
        boolean isKeyPlayer  // true para QBs, WR1, RB1, astros defensivos
) {}