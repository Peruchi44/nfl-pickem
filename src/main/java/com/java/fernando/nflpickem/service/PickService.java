package com.java.fernando.nflpickem.service;

import com.java.fernando.nflpickem.dto.PickRequest;
import com.java.fernando.nflpickem.model.Game;
import com.java.fernando.nflpickem.model.UserPick;
import com.java.fernando.nflpickem.model.Week;
import com.java.fernando.nflpickem.repository.UserPickRepository;
import com.java.fernando.nflpickem.repository.WeekRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PickService {

    private final UserPickRepository pickRepository;
    private final WeekRepository weekRepository;

    public PickService(UserPickRepository pickRepository, WeekRepository weekRepository) {
        this.pickRepository = pickRepository;
        this.weekRepository = weekRepository;
    }

    public UserPick submitPicks(PickRequest request) {
        Week week = weekRepository.findBySeasonAndWeekNumber(request.season(), request.weekNumber())
                .orElseThrow(() -> new IllegalArgumentException("Semana não encontrada"));

        Map<String, Game> gameMap = week.getGames().stream()
                .collect(Collectors.toMap(Game::getId, g -> g));

        Instant now = Instant.now();

        // Validação: impede palpite se o jogo já começou
        for (Map.Entry<String, String> entry : request.picks().entrySet()) {
            Game game = gameMap.get(entry.getKey());
            if (game != null && game.getKickOff() != null && now.isAfter(game.getKickOff())) {
                throw excitingGameAlreadyStartedException(game.getId());
            }
        }

        UserPick userPick = pickRepository.findByUserIdAndSeasonAndWeekNumber(
                        request.userId(), request.season(), request.weekNumber())
                .orElseGet(() -> {
                    UserPick up = new UserPick();
                    up.setUserId(request.userId());
                    up.setSeason(request.season());
                    up.setWeekNumber(request.weekNumber());
                    return up;
                });

        userPick.setPicks(request.picks());
        userPick.setUpdatedAt(Instant.now());

        return pickRepository.save(userPick);
    }

    public Optional<UserPick> getUserPicks(String userId, int season, int weekNumber) {
        return pickRepository.findByUserIdAndSeasonAndWeekNumber(userId, season, weekNumber);
    }

    public List<UserPick> getAllPicksForWeek(int season, int weekNumber) {
        return pickRepository.findBySeasonAndWeekNumber(season, weekNumber);
    }

    private RuntimeException excitingGameAlreadyStartedException(String gameId) {
        return new IllegalStateException("O jogo " + gameId + " já iniciou. Palpites bloqueados.");
    }
}