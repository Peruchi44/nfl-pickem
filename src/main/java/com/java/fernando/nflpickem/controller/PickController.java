package com.java.fernando.nflpickem.controller;

import com.java.fernando.nflpickem.dto.PickRequest;
import com.java.fernando.nflpickem.model.UserPick;
import com.java.fernando.nflpickem.service.PickService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/picks")
@CrossOrigin(origins = "*")
public class PickController {

    private final PickService pickService;

    public PickController(PickService pickService) {
        this.pickService = pickService;
    }

    @PostMapping
    public ResponseEntity<?> submitPicks(@Valid @RequestBody PickRequest request) {
        try {
            UserPick pick = pickService.submitPicks(request);
            return ResponseEntity.ok(pick);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/{season}/{weekNumber}")
    public ResponseEntity<UserPick> getUserPicks(
            @PathVariable String userId,
            @PathVariable int season,
            @PathVariable int weekNumber) {
        return pickService.getUserPicks(userId, season, weekNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{season}/{weekNumber}")
    public ResponseEntity<List<UserPick>> getAllPicksForWeek(
            @PathVariable int season,
            @PathVariable int weekNumber) {
        return ResponseEntity.ok(pickService.getAllPicksForWeek(season, weekNumber));
    }
}