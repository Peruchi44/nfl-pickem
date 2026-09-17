package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.model.Week;
import com.java.fernando.nflpickem.service.EspnSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    private final EspnSyncService espnSyncService;

    public SyncController(EspnSyncService espnSyncService) {
        this.espnSyncService = espnSyncService;
    }

    @PostMapping("/{season}/{weekNumber}")
    public ResponseEntity<Week> syncWeek(@PathVariable int season, @PathVariable int weekNumber) {
        Week week = espnSyncService.syncWeek(season, weekNumber);
        return ResponseEntity.ok(week);
    }
}