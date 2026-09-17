package com.java.fernando.nflpickem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fernando.nflpickem.model.Game;
import com.java.fernando.nflpickem.model.Week;
import com.java.fernando.nflpickem.repository.WeekRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class EspnSyncService {

    private static final Logger log = LoggerFactory.getLogger(EspnSyncService.class);

    private final WeekRepository weekRepository;
    private final LeaderBoardService leaderboardService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cliente HTTP nativo do Java moderno
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public EspnSyncService(WeekRepository weekRepository, LeaderBoardService leaderboardService) {
        this.weekRepository = weekRepository;
        this.leaderboardService = leaderboardService;
    }

    /**
     * Roda a cada 15 minutos (900.000 ms).
     * Consulta a ESPN sem parâmetros para descobrir a temporada/semana vigentes automaticamente.
     */
    @Scheduled(fixedDelay = 900000, initialDelay = 5000)
    public void scheduledSyncCurrentWeek() {
        try {
            log.info("Iniciando sincronização automática da NFL via ESPN...");
            String currentUrl = "https://cdn.espn.com/core/nfl/schedule?xhr=1";
            String jsonResponse = fetchHttp(currentUrl);

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode content = root.path("content");

            int activeSeason = content.path("parameters").path("year").asInt(0);
            int activeWeek = content.path("parameters").path("week").asInt(0);

            if (activeSeason == 0 || activeWeek == 0) {
                activeSeason = content.path("season").path("year").asInt(2026);
                activeWeek = content.path("week").path("number").asInt(1);
            }

            log.info("Rodada ativa identificada na ESPN: Temporada {} - Semana {}", activeSeason, activeWeek);
            syncWeek(activeSeason, activeWeek);
        } catch (Exception e) {
            log.error("Falha ao sincronizar semana corrente da NFL: {}", e.getMessage());
        }
    }

    public Week syncWeek(int season, int weekNumber) {
        String url = String.format(
                "https://cdn.espn.com/core/nfl/schedule?xhr=1&year=%d&seasontype=2&week=%d",
                season, weekNumber
        );

        String jsonResponse = fetchHttp(url);
        JsonNode root;
        try {
            root = objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao processar resposta JSON da ESPN: " + e.getMessage(), e);
        }

        JsonNode scheduleNode = root.path("content").path("schedule");
        if (scheduleNode.isMissingNode() || scheduleNode.isEmpty()) {
            throw new IllegalStateException("Grade de jogos não encontrada para a semana " + weekNumber);
        }

        // Garante que semanas antigas não fiquem como current = true
        weekRepository.findAll().forEach(w -> {
            if (w.isCurrent() && (w.getSeason() != season || w.getWeekNumber() != weekNumber)) {
                w.setCurrent(false);
                weekRepository.save(w);
            }
        });

        Week week = weekRepository.findBySeasonAndWeekNumber(season, weekNumber)
                .orElseGet(() -> {
                    Week w = new Week();
                    w.setSeason(season);
                    w.setWeekNumber(weekNumber);
                    w.setLabel("Semana " + weekNumber);
                    return w;
                });

        week.setCurrent(true);

        List<Game> games = new ArrayList<>();
        Iterator<JsonNode> dateEntries = scheduleNode.elements();
        while (dateEntries.hasNext()) {
            JsonNode dateGroup = dateEntries.next();
            JsonNode gamesList = dateGroup.path("games");

            if (gamesList.isArray()) {
                for (JsonNode gameNode : gamesList) {
                    Game game = new Game();
                    game.setId(gameNode.path("id").asText());

                    String dateStr = gameNode.path("date").asText(null);
                    if (dateStr == null || dateStr.isBlank()) {
                        dateStr = gameNode.path("startDate").asText(null);
                    }

                    if (dateStr != null && !dateStr.isBlank()) {
                        try {
                            game.setKickOff(java.time.ZonedDateTime.parse(dateStr).toInstant());
                        } catch (Exception ignored) {}
                    }

                    String statusName = gameNode.path("status").path("type").path("name").asText("STATUS_SCHEDULED");
                    if ("STATUS_FINAL".equalsIgnoreCase(statusName)) {
                        game.setStatus("FINAL");
                    } else if ("STATUS_IN_PROGRESS".equalsIgnoreCase(statusName)) {
                        game.setStatus("IN_PROGRESS");
                    } else {
                        game.setStatus("SCHEDULED");
                    }

                    JsonNode competitions = gameNode.path("competitions");
                    if (competitions.isArray() && !competitions.isEmpty()) {
                        JsonNode competition = competitions.get(0);
                        JsonNode competitors = competition.path("competitors");

                        for (JsonNode competitor : competitors) {
                            boolean isHome = "home".equalsIgnoreCase(competitor.path("homeAway").asText());
                            String teamAbbr = competitor.path("team").path("abbreviation").asText();
                            String teamLogo = competitor.path("team").path("logo").asText();
                            int score = competitor.path("score").asInt(0);
                            boolean isWinner = competitor.path("winner").asBoolean(false);

                            if (isHome) {
                                game.setHomeTeam(teamAbbr);
                                game.setHomeLogo(teamLogo);
                                if ("FINAL".equals(game.getStatus())) {
                                    game.setHomeScore(score);
                                }
                            } else {
                                game.setAwayTeam(teamAbbr);
                                game.setAwayLogo(teamLogo);
                                if ("FINAL".equals(game.getStatus())) {
                                    game.setAwayScore(score);
                                }
                            }

                            if (isWinner && "FINAL".equals(game.getStatus())) {
                                game.setWinnerTeam(teamAbbr);
                            }
                        }
                    }

                    games.add(game);
                }
            }
        }
        if (!games.isEmpty() && (week.getTiebreakerGameId() == null || week.getTiebreakerGameId().isBlank())) {
            week.setTiebreakerGameId(games.get(games.size() - 1).getId());
        }

        week.setGames(games);
        Week savedWeek = weekRepository.save(week);

        // Recalcula o placar do grupo logo após a atualização
        try {
            leaderboardService.calculateWeekScores(season, weekNumber);
        } catch (Exception e) {
            log.warn("Aviso ao calcular leaderboard da semana {}: {}", weekNumber, e.getMessage());
        }
        return savedWeek;
    }

    private String fetchHttp(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("ESPN respondeu com status HTTP " + response.statusCode());
            }

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao consultar API da ESPN: " + e.getMessage(), e);
        }
    }
}