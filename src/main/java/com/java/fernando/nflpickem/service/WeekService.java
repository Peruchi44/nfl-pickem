package com.java.fernando.nflpickem.service;

import com.java.fernando.nflpickem.model.Game;
import com.java.fernando.nflpickem.model.Week;
import com.java.fernando.nflpickem.repository.WeekRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class WeekService {

    private final WeekRepository weekRepository;

    public WeekService(WeekRepository weekRepository) {
        this.weekRepository = weekRepository;
    }

    public Optional<Week> getCurrentWeek() {
        return weekRepository.findByCurrentTrue();
    }

    public Optional<Week> getWeek(int season, int weekNumber) {
        return weekRepository.findBySeasonAndWeekNumber(season, weekNumber);
    }

    public Week seedWeekOne() {
        int season = 2026;
        int weekNumber = 1;

        Optional<Week> existing = weekRepository.findBySeasonAndWeekNumber(season, weekNumber);
        if (existing.isPresent()) {
            return existing.get();
        }

        Week week = new Week();
        week.setSeason(season);
        week.setWeekNumber(weekNumber);
        week.setLabel("Semana 1");
        week.setCurrent(true);

        Game g1 = new Game();
        g1.setId("game-1");
        g1.setHomeTeam("KC");
        g1.setAwayTeam("BAL");
        g1.setHomeLogo("https://a.espncdn.com/i/teamlogos/nfl/500/kc.png");
        g1.setAwayLogo("https://a.espncdn.com/i/teamlogos/nfl/500/bal.png");
        g1.setKickOff(Instant.now().plus(2, ChronoUnit.DAYS));
        g1.setStatus("SCHEDULED");

        Game g2 = new Game();
        g2.setId("game-2");
        g2.setHomeTeam("PHI");
        g2.setAwayTeam("GB");
        g2.setHomeLogo("https://a.espncdn.com/i/teamlogos/nfl/500/phi.png");
        g2.setAwayLogo("https://a.espncdn.com/i/teamlogos/nfl/500/gb.png");
        g2.setKickOff(Instant.now().plus(3, ChronoUnit.DAYS));
        g2.setStatus("SCHEDULED");

        Game g3 = new Game();
        g3.setId("game-3");
        g3.setHomeTeam("DAL");
        g3.setAwayTeam("CLE");
        g3.setHomeLogo("https://a.espncdn.com/i/teamlogos/nfl/500/dal.png");
        g3.setAwayLogo("https://a.espncdn.com/i/teamlogos/nfl/500/cle.png");
        g3.setKickOff(Instant.now().plus(4, ChronoUnit.DAYS));
        g3.setStatus("SCHEDULED");

        week.setGames(List.of(g1, g2, g3));

        return weekRepository.save(week);
    }
}