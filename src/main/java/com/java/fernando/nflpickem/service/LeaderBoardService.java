package com.java.fernando.nflpickem.service;

import com.java.fernando.nflpickem.dto.LeaderBoardEntry;
import com.java.fernando.nflpickem.model.Game;
import com.java.fernando.nflpickem.model.User;
import com.java.fernando.nflpickem.model.UserPick;
import com.java.fernando.nflpickem.model.Week;
import com.java.fernando.nflpickem.repository.UserRepository;
import com.java.fernando.nflpickem.repository.UserPickRepository;
import com.java.fernando.nflpickem.repository.WeekRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeaderBoardService {

    private final UserRepository userRepository;
    private final UserPickRepository userPickRepository;
    private final WeekRepository weekRepository;

    private static final int CURRENT_SEASON = 2026;

    /**
     * Recalcula e persiste as pontuações dos palpites da semana após a sincronização da ESPN
     */
    public void calculateWeekScores(int season, int weekNumber) {
        Week week = weekRepository.findBySeasonAndWeekNumber(season, weekNumber).orElse(null);
        if (week == null || week.getGames() == null || week.getGames().isEmpty()) {
            return;
        }

        Map<String, Game> gamesMap = new HashMap<>();
        week.getGames().forEach(g -> gamesMap.put(g.getId(), g));

        List<UserPick> weekPicks = userPickRepository.findBySeasonAndWeekNumber(season, weekNumber);

        for (UserPick userPick : weekPicks) {
            int score = 0;
            if (userPick.getPicks() != null) {
                for (Map.Entry<String, String> entry : userPick.getPicks().entrySet()) {
                    String gameId = entry.getKey();
                    String chosenTeam = entry.getValue();

                    Game game = gamesMap.get(gameId);
                    if (game != null && isGameFinished(game)) {
                        String winner = getGameWinner(game);
                        if (winner != null && winner.equalsIgnoreCase(chosenTeam)) {
                            score++;
                        }
                    }
                }
            }
            userPick.setTotalPoints(score);
            userPick.setUpdatedAt(Instant.now());
            userPickRepository.save(userPick);
        }
    }

    /**
     * Tabela de Classificação Semanal (ex: Semana 6)
     */
    public List<LeaderBoardEntry> getWeeklyLeaderboard(int season, int weekNumber) {
        List<User> users = userRepository.findAll();
        Week week = weekRepository.findBySeasonAndWeekNumber(season, weekNumber).orElse(null);

        Map<String, Game> gamesMap = new HashMap<>();
        String tiebreakerGameId = null;

        if (week != null && week.getGames() != null) {
            week.getGames().forEach(g -> gamesMap.put(g.getId(), g));
            tiebreakerGameId = week.getTiebreakerGameId();
            if (tiebreakerGameId == null && !week.getGames().isEmpty()) {
                tiebreakerGameId = week.getGames().get(week.getGames().size() - 1).getId();
            }
        }

        // Calcula a margem real se o jogo do tiebreaker já tiver terminado
        Integer actualMargin = null;
        if (tiebreakerGameId != null && gamesMap.containsKey(tiebreakerGameId)) {
            Game tbGame = gamesMap.get(tiebreakerGameId);
            if (isGameFinished(tbGame) && tbGame.getHomeScore() != null && tbGame.getAwayScore() != null) {
                actualMargin = Math.abs(tbGame.getHomeScore() - tbGame.getAwayScore());
            }
        }

        List<LeaderBoardEntry> entries = new ArrayList<>();

        for (User user : users) {
            UserPick userPick = userPickRepository
                    .findByUserIdAndSeasonAndWeekNumber(user.getId(), season, weekNumber)
                    .orElse(null);

            int correctPicks = 0;
            Integer tiebreakerDiff = null;

            if (userPick != null) {
                if (userPick.getPicks() != null) {
                    for (Map.Entry<String, String> entry : userPick.getPicks().entrySet()) {
                        Game game = gamesMap.get(entry.getKey());
                        if (game != null && isGameFinished(game)) {
                            String realWinner = getGameWinner(game);
                            if (realWinner != null && realWinner.equalsIgnoreCase(entry.getValue())) {
                                correctPicks++;
                            }
                        }
                    }
                }

                if (actualMargin != null && userPick.getTiebreakerMargin() != null) {
                    tiebreakerDiff = Math.abs(userPick.getTiebreakerMargin() - actualMargin);
                }
            }

            entries.add(new LeaderBoardEntry(
                    user.getId(),
                    getUserDisplayName(user),
                    correctPicks,
                    0,
                    tiebreakerDiff
            ));
        }

        return sortAndAssignRank(entries);
    }

    /**
     * Tabela de Classificação Geral da Época
     */
    public List<LeaderBoardEntry> getSeasonLeaderboard(int season) {
        List<User> users = userRepository.findAll();
        List<Week> weeks = weekRepository.findBySeason(season);

        Map<Integer, Week> weeksMap = new HashMap<>();
        if (weeks != null) {
            weeks.forEach(w -> weeksMap.put(w.getWeekNumber(), w));
        }

        List<LeaderBoardEntry> entries = new ArrayList<>();

        for (User user : users) {
            List<UserPick> allUserPicks = userPickRepository.findByUserIdAndSeason(user.getId(), season);

            int totalCorrect = 0;
            int totalTiebreakerDiff = 0;
            boolean hasComputedTiebreaker = false;

            for (UserPick userPick : allUserPicks) {
                Week week = weeksMap.get(userPick.getWeekNumber());
                if (week == null || week.getGames() == null) continue;

                Map<String, Game> weekGamesMap = new HashMap<>();
                week.getGames().forEach(g -> weekGamesMap.put(g.getId(), g));

                // Acertos nos jogos
                if (userPick.getPicks() != null) {
                    for (Map.Entry<String, String> entry : userPick.getPicks().entrySet()) {
                        Game game = weekGamesMap.get(entry.getKey());
                        if (game != null && isGameFinished(game)) {
                            String realWinner = getGameWinner(game);
                            if (realWinner != null && realWinner.equalsIgnoreCase(entry.getValue())) {
                                totalCorrect++;
                            }
                        }
                    }
                }

                // Desempate do jogo final
                String tbGameId = week.getTiebreakerGameId();
                if (tbGameId == null && !week.getGames().isEmpty()) {
                    tbGameId = week.getGames().get(week.getGames().size() - 1).getId();
                }

                if (tbGameId != null && weekGamesMap.containsKey(tbGameId)) {
                    Game tbGame = weekGamesMap.get(tbGameId);
                    if (isGameFinished(tbGame) && tbGame.getHomeScore() != null && tbGame.getAwayScore() != null) {
                        if (userPick.getTiebreakerMargin() != null) {
                            int actualMargin = Math.abs(tbGame.getHomeScore() - tbGame.getAwayScore());
                            totalTiebreakerDiff += Math.abs(userPick.getTiebreakerMargin() - actualMargin);
                            hasComputedTiebreaker = true;
                        }
                    }
                }
            }

            Integer finalDiff = hasComputedTiebreaker ? totalTiebreakerDiff : null;

            entries.add(new LeaderBoardEntry(
                    user.getId(),
                    getUserDisplayName(user),
                    totalCorrect,
                    0,
                    finalDiff
            ));
        }

        return sortAndAssignRank(entries);
    }

    public List<LeaderBoardEntry> getSeasonLeaderboard() {
        return getSeasonLeaderboard(CURRENT_SEASON);
    }

    private boolean isGameFinished(Game game) {
        if (game == null) return false;
        String status = game.getStatus();
        return "STATUS_FINAL".equalsIgnoreCase(status)
                || "FINAL".equalsIgnoreCase(status)
                || game.getWinnerTeam() != null;
    }

    private String getGameWinner(Game game) {
        if (game.getWinnerTeam() != null && !game.getWinnerTeam().isBlank()) {
            return game.getWinnerTeam().trim();
        }
        if (game.getHomeScore() != null && game.getAwayScore() != null) {
            if (game.getHomeScore() > game.getAwayScore()) return game.getHomeTeam();
            if (game.getAwayScore() > game.getHomeScore()) return game.getAwayTeam();
            return "TIE";
        }
        return null;
    }

    private String getUserDisplayName(User user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) return user.getDisplayName();
        if (user.getName() != null && !user.getName().isBlank()) return user.getName();
        return user.getEmail();
    }

    private List<LeaderBoardEntry> sortAndAssignRank(List<LeaderBoardEntry> entries) {
        // Ordena por acertos (descendente) e em caso de empate pelo tiebreaker (ascendente)
        entries.sort(
                Comparator.comparingInt(LeaderBoardEntry::totalPoints).reversed()
                        .thenComparing(
                                LeaderBoardEntry::tiebreakerDiff,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
        );

        List<LeaderBoardEntry> rankedList = new ArrayList<>();
        int currentRank = 1;

        for (LeaderBoardEntry entry : entries) {
            rankedList.add(new LeaderBoardEntry(
                    entry.userId(),
                    entry.displayName(),
                    entry.totalPoints(),
                    currentRank++,
                    entry.tiebreakerDiff()
            ));
        }

        return rankedList;
    }
}