package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.dto.MatchupInsightDTO;
import com.java.fernando.nflpickem.service.MatchupInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InsightController {

    private final MatchupInsightService insightService;

    @GetMapping("/game/{gameId}")
    public ResponseEntity<MatchupInsightDTO> getGameInsight(
            @PathVariable String gameId,
            @RequestParam String away,
            @RequestParam String home) {
        return ResponseEntity.ok(insightService.getGameInsight(gameId, away, home));
    }
}