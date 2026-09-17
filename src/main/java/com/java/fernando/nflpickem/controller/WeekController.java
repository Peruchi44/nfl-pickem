package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.model.Week;
import com.java.fernando.nflpickem.repository.WeekRepository;
import com.java.fernando.nflpickem.service.EspnSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weeks")
@CrossOrigin(origins = "*")
public class WeekController {

    private final WeekRepository weekRepository;
    private final EspnSyncService espnSyncService;

    public WeekController(WeekRepository weekRepository, EspnSyncService espnSyncService) {
        this.weekRepository = weekRepository;
        this.espnSyncService = espnSyncService;
    }

    @GetMapping("/{season}/{weekNumber}")
    public ResponseEntity<Week> getWeek(@PathVariable int season, @PathVariable int weekNumber) {
        return weekRepository.findBySeasonAndWeekNumber(season, weekNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/current")
    public ResponseEntity<Week> getCurrentWeek() {
        return weekRepository.findByCurrentTrue()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Rota para sincronizar manualmente qualquer semana
    @PostMapping("/sync/{season}/{weekNumber}")
    public ResponseEntity<Week> syncWeekManual(@PathVariable int season, @PathVariable int weekNumber) {
        Week syncedWeek = espnSyncService.syncWeek(season, weekNumber);
        return ResponseEntity.ok(syncedWeek);
    }
}