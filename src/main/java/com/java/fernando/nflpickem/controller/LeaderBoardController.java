package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.dto.LeaderBoardEntry;
import com.java.fernando.nflpickem.service.LeaderBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeaderBoardController {

    private final LeaderBoardService leaderBoardService;

    @GetMapping("/season")
    public ResponseEntity<List<LeaderBoardEntry>> getSeasonLeaderboard() {
        return ResponseEntity.ok(leaderBoardService.getSeasonLeaderboard());
    }

    @GetMapping("/week/{weekNumber}")
    public ResponseEntity<List<LeaderBoardEntry>> getWeeklyLeaderboard(
            @RequestParam(defaultValue = "2026") int season,
            @PathVariable int weekNumber) {
        return ResponseEntity.ok(leaderBoardService.getWeeklyLeaderboard(season, weekNumber));
    }
}