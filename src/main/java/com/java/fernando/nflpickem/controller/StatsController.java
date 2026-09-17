package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.dto.TeamStatsDto;
import com.java.fernando.nflpickem.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/{season}/before-week/{weekNumber}")
    public ResponseEntity<Map<String, TeamStatsDto>> getStatsBeforeWeek(
            @PathVariable int season,
            @PathVariable int weekNumber) {
        return ResponseEntity.ok(statsService.calculateStatsBeforeWeek(season, weekNumber));
    }
}