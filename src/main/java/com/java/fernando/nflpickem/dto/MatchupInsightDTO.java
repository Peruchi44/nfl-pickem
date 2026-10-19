package com.java.fernando.nflpickem.dto;

import java.util.List;

public record MatchupInsightDTO(
        String gameId,
        String awayTeam,
        String homeTeam,
        QbStatsDTO awayQb,
        QbStatsDTO homeQb,
        List<InjuryReportDTO> injuries
) {}