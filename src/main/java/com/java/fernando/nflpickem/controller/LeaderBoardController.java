package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.dto.LeaderBoardEntry;
import com.java.fernando.nflpickem.service.LeaderBoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderBoardController {

    private final LeaderBoardService leaderboardService;

    public LeaderBoardController(LeaderBoardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/{season}")
    public ResponseEntity<List<LeaderBoardEntry>> getLeaderboard(@PathVariable int season) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(season));
    }

    @PostMapping("/calculate/{season}/{weekNumber}")
    public ResponseEntity<String> calculatePoints(@PathVariable int season, @PathVariable int weekNumber) {
        leaderboardService.calculateWeekScores(season, weekNumber);
        return ResponseEntity.ok("Pontuações calculadas com sucesso!");
    }
}