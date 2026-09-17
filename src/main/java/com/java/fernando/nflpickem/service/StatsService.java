package com.java.fernando.nflpickem.service;

import com.java.fernando.nflpickem.dto.TeamStatsDto;
import com.java.fernando.nflpickem.model.Game;
import com.java.fernando.nflpickem.model.Week;
import com.java.fernando.nflpickem.repository.WeekRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatsService {

    private final WeekRepository weekRepository;

    public StatsService(WeekRepository weekRepository) {
        this.weekRepository = weekRepository;
    }

    public Map<String, TeamStatsDto> calculateStatsBeforeWeek(int season, int targetWeek) {
        // Busca todas as semanas anteriores da mesma temporada
        List<Week> previousWeeks = weekRepository.findBySeason(season).stream()
                .filter(w -> w.getWeekNumber() < targetWeek)
                .sorted(Comparator.comparingInt(Week::getWeekNumber))
                .toList();

        // Armazena acumuladores temporários para cada time
        Map<String, TeamAccumulator> accMap = new HashMap<>();

        for (Week week : previousWeeks) {
            if (week.getGames() == null) continue;

            for (Game game : week.getGames()) {
                // Considera apenas jogos finalizados com placar
                if (!"STATUS_FINAL".equalsIgnoreCase(game.getStatus()) && !"FINAL".equalsIgnoreCase(game.getStatus())) {
                    continue;
                }

                String home = game.getHomeTeam();
                String away = game.getAwayTeam();
                int homeScore = game.getHomeScore();
                int awayScore = game.getAwayScore();

                TeamAccumulator homeAcc = accMap.computeIfAbsent(home, k -> new TeamAccumulator(home));
                TeamAccumulator awayAcc = accMap.computeIfAbsent(away, k -> new TeamAccumulator(away));

                homeAcc.gamesPlayed++;
                awayAcc.gamesPlayed++;

                homeAcc.pointsFor += homeScore;
                homeAcc.pointsAgainst += awayScore;

                awayAcc.pointsFor += awayScore;
                awayAcc.pointsAgainst += homeScore;

                if (homeScore > awayScore) {
                    homeAcc.wins++;
                    awayAcc.losses++;
                    homeAcc.recentResults.add(String.format("V %d-%d vs %s (Sem %d)", homeScore, awayScore, away, week.getWeekNumber()));
                    awayAcc.recentResults.add(String.format("D %d-%d @ %s (Sem %d)", awayScore, homeScore, home, week.getWeekNumber()));
                } else if (awayScore > homeScore) {
                    awayAcc.wins++;
                    homeAcc.losses++;
                    awayAcc.recentResults.add(String.format("V %d-%d @ %s (Sem %d)", awayScore, homeScore, home, week.getWeekNumber()));
                    homeAcc.recentResults.add(String.format("D %d-%d vs %s (Sem %d)", homeScore, awayScore, away, week.getWeekNumber()));
                } else {
                    homeAcc.ties++;
                    awayAcc.ties++;
                    homeAcc.recentResults.add(String.format("E %d-%d vs %s (Sem %d)", homeScore, awayScore, away, week.getWeekNumber()));
                    awayAcc.recentResults.add(String.format("E %d-%d @ %s (Sem %d)", awayScore, homeScore, home, week.getWeekNumber()));
                }
            }
        }

        // Converte acumuladores para os DTOs finais com médias calculadas
        Map<String, TeamStatsDto> response = new HashMap<>();
        for (Map.Entry<String, TeamAccumulator> entry : accMap.entrySet()) {
            TeamAccumulator acc = entry.getValue();
            double ppg = acc.gamesPlayed > 0 ? (double) acc.pointsFor / acc.gamesPlayed : 0.0;
            double oppg = acc.gamesPlayed > 0 ? (double) acc.pointsAgainst / acc.gamesPlayed : 0.0;

            // Arredonda para 1 casa decimal
            ppg = Math.round(ppg * 10.0) / 10.0;
            oppg = Math.round(oppg * 10.0) / 10.0;

            response.put(entry.getKey(), new TeamStatsDto(
                    acc.teamName,
                    acc.gamesPlayed,
                    acc.wins,
                    acc.losses,
                    acc.ties,
                    ppg,
                    oppg,
                    acc.recentResults
            ));
        }

        return response;
    }

    private static class TeamAccumulator {
        String teamName;
        int gamesPlayed = 0;
        int wins = 0;
        int losses = 0;
        int ties = 0;
        int pointsFor = 0;
        int pointsAgainst = 0;
        List<String> recentResults = new ArrayList<>();

        TeamAccumulator(String teamName) {
            this.teamName = teamName;
        }
    }
}