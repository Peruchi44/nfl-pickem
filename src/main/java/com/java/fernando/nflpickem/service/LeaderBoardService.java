package com.java.fernando.nflpickem.service;

import com.java.fernando.nflpickem.dto.LeaderBoardEntry;
import com.java.fernando.nflpickem.model.Game;
import com.java.fernando.nflpickem.model.User;
import com.java.fernando.nflpickem.model.UserPick;
import com.java.fernando.nflpickem.model.Week;
import com.java.fernando.nflpickem.repository.UserPickRepository;
import com.java.fernando.nflpickem.repository.UserRepository;
import com.java.fernando.nflpickem.repository.WeekRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderBoardService {

    private final UserPickRepository pickRepository;
    private final WeekRepository weekRepository;
    private final UserRepository userRepository;

    public LeaderBoardService(UserPickRepository pickRepository,
                              WeekRepository weekRepository,
                              UserRepository userRepository) {
        this.pickRepository = pickRepository;
        this.weekRepository = weekRepository;
        this.userRepository = userRepository;
    }

    public void calculateWeekScores(int season, int weekNumber) {
        Week week = weekRepository.findBySeasonAndWeekNumber(season, weekNumber)
                .orElseThrow(() -> new IllegalArgumentException("Semana não encontrada"));

        Map<String, String> winners = new HashMap<>();
        if (week.getGames() != null) {
            for (Game g : week.getGames()) {
                if ("FINAL".equalsIgnoreCase(g.getStatus()) && g.getWinnerTeam() != null && g.getId() != null) {
                    winners.put(g.getId(), g.getWinnerTeam());
                }
            }
        }

        List<UserPick> weekPicks = pickRepository.findBySeasonAndWeekNumber(season, weekNumber);

        for (UserPick pick : weekPicks) {
            int score = 0;
            if (pick.getPicks() != null) {
                for (Map.Entry<String, String> userChoice : pick.getPicks().entrySet()) {
                    String winningTeam = winners.get(userChoice.getKey());
                    if (winningTeam != null && winningTeam.equalsIgnoreCase(userChoice.getValue())) {
                        score++;
                    }
                }
            }
            pick.setTotalPoints(score);
            pickRepository.save(pick);
        }
    }

    public List<LeaderBoardEntry> getLeaderboard(int season) {
        List<User> users = userRepository.findAll();
        Map<String, String> userNames = users.stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));

        List<Week> weeks = weekRepository.findAll().stream()
                .filter(w -> w.getSeason() == season)
                .toList();

        // Mapeia as margens reais dos jogos de tiebreaker finalizados
        Map<String, Integer> realTiebreakerMargins = new HashMap<>();
        for (Week w : weeks) {
            if (w.getTiebreakerGameId() != null && w.getGames() != null) {
                for (Game g : w.getGames()) {
                    if (w.getTiebreakerGameId().equals(g.getId()) && "FINAL".equalsIgnoreCase(g.getStatus())) {
                        int margin = Math.abs(g.getHomeScore() - g.getAwayScore());
                        realTiebreakerMargins.put(w.getTiebreakerGameId(), margin);
                    }
                }
            }
        }

        List<UserPick> allPicks = pickRepository.findAll().stream()
                .filter(p -> p.getSeason() == season)
                .toList();

        Map<String, Integer> scoreByUser = new HashMap<>();
        Map<String, Integer> tiebreakerDiffByUser = new HashMap<>();

        for (User u : users) {
            scoreByUser.put(u.getId(), 0);
            tiebreakerDiffByUser.put(u.getId(), 0);
        }

        for (UserPick pick : allPicks) {
            scoreByUser.merge(pick.getUserId(), pick.getTotalPoints(), Integer::sum);

            // Calcula erro do desempate se a semana já teve o jogo encerrado
            weeks.stream()
                    .filter(w -> w.getWeekNumber() == pick.getWeekNumber())
                    .findFirst()
                    .ifPresent(w -> {
                        String tGameId = w.getTiebreakerGameId();
                        if (tGameId != null && realTiebreakerMargins.containsKey(tGameId) && pick.getTiebreakerMargin() != null) {
                            int actualMargin = realTiebreakerMargins.get(tGameId);
                            int diff = Math.abs(pick.getTiebreakerMargin() - actualMargin);
                            tiebreakerDiffByUser.merge(pick.getUserId(), diff, Integer::sum);
                        }
                    });
        }

        // Ordenação: 1º Maior pontuação | 2º Menor erro no tiebreaker
        List<String> sortedUserIds = new ArrayList<>(users.stream().map(User::getId).toList());
        sortedUserIds.sort((u1, u2) -> {
            int scoreCompare = Integer.compare(scoreByUser.getOrDefault(u2, 0), scoreByUser.getOrDefault(u1, 0));
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return Integer.compare(tiebreakerDiffByUser.getOrDefault(u1, 0), tiebreakerDiffByUser.getOrDefault(u2, 0));
        });

        List<LeaderBoardEntry> rankedList = new ArrayList<>();
        int currentRank = 1;
        for (String uId : sortedUserIds) {
            String displayName = userNames.getOrDefault(uId, "Anônimo");
            int score = scoreByUser.getOrDefault(uId, 0);
            rankedList.add(new LeaderBoardEntry(uId, displayName, score, currentRank++));
        }

        return rankedList;
    }
}