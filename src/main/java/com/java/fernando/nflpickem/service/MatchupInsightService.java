package com.java.fernando.nflpickem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.fernando.nflpickem.dto.InjuryReportDTO;
import com.java.fernando.nflpickem.dto.MatchupInsightDTO;
import com.java.fernando.nflpickem.dto.QbStatsDTO;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchupInsightService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient httpClient = new OkHttpClient();

    // Posições com impacto crítico para ativar o destaque visual
    private static final Set<String> KEY_POSITIONS = Set.of("QB", "WR", "RB", "TE", "DE", "EDGE", "CB");

    public MatchupInsightDTO getGameInsight(String gameId, String awayTeam, String homeTeam) {
        List<InjuryReportDTO> injuries = new ArrayList<>();
        QbStatsDTO awayQb = null;
        QbStatsDTO homeQb = null;

        // O endpoint de sumário da ESPN fornece rosters, lesões e estatísticas do confronto
        String espnSummaryUrl = "https://site.api.espn.com/apis/site/v2/sports/football/nfl/summary?event=" + gameId;

        try {
            Request request = new Request.Builder().url(espnSummaryUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = objectMapper.readTree(response.body().string());

                    // 1. Extração do Relatório de Lesões
                    if (root.has("injuries")) {
                        for (JsonNode teamInjuryNode : root.get("injuries")) {
                            String teamAbbr = teamInjuryNode.path("team").path("abbreviation").asText("");
                            if (teamInjuryNode.has("injuries")) {
                                for (JsonNode injNode : teamInjuryNode.get("injuries")) {
                                    String athleteName = injNode.path("athlete").path("displayName").asText("Jogador");
                                    String position = injNode.path("athlete").path("position").path("abbreviation").asText("N/A");
                                    String status = injNode.path("status").asText("QUESTIONABLE").toUpperCase();
                                    String detail = injNode.path("details").path("type").asText("Não informado");

                                    boolean isKey = KEY_POSITIONS.contains(position.toUpperCase());

                                    injuries.add(new InjuryReportDTO(
                                            athleteName,
                                            position,
                                            teamAbbr,
                                            detail,
                                            status,
                                            isKey
                                    ));
                                }
                            }
                        }
                    }

                    // 2. Extração dos Dados dos QBs
                    if (root.has("boxscore") && root.path("boxscore").has("players")) {
                        for (JsonNode teamBox : root.path("boxscore").get("players")) {
                            String teamAbbr = teamBox.path("team").path("abbreviation").asText("");
                            JsonNode passingCategory = null;

                            for (JsonNode statCat : teamBox.path("statistics")) {
                                if ("passing".equalsIgnoreCase(statCat.path("name").asText())) {
                                    passingCategory = statCat;
                                    break;
                                }
                            }

                            if (passingCategory != null && passingCategory.has("athletes") && !passingCategory.get("athletes").isEmpty()) {
                                JsonNode qbNode = passingCategory.get("athletes").get(0); // QB titular com maior volume
                                String qbName = qbNode.path("athlete").path("displayName").asText();
                                String headshot = qbNode.path("athlete").path("headshot").path("href")
                                        .asText("https://a.espncdn.com/combiner/i?img=/i/headshots/nfl/players/full/default.png");

                                // Ordem habitual das estatísticas da ESPN: C/ATT, YDS, AVG, TD, INT, SACKS, RTG
                                JsonNode statsArray = qbNode.path("stats");
                                int yards = statsArray.size() > 1 ? parseStatInt(statsArray.get(1).asText()) : 0;
                                int tds = statsArray.size() > 3 ? parseStatInt(statsArray.get(3).asText()) : 0;
                                int ints = statsArray.size() > 4 ? parseStatInt(statsArray.get(4).asText()) : 0;
                                double rtg = statsArray.size() > 6 ? parseStatDouble(statsArray.get(6).asText()) : 85.0;

                                QbStatsDTO qbStats = new QbStatsDTO(
                                        qbName,
                                        teamAbbr,
                                        headshot,
                                        yards,
                                        tds,
                                        ints,
                                        65.0, // Taxa de conclusão estimada ou calculada
                                        rtg
                                );

                                if (teamAbbr.equalsIgnoreCase(awayTeam)) {
                                    awayQb = qbStats;
                                } else if (teamAbbr.equalsIgnoreCase(homeTeam)) {
                                    homeQb = qbStats;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Em caso de falha de conexão na API externa, retorna fallback com dados vazios
        }

        // Se o confronto ainda não iniciou ou a boxscore estiver indisponível, cria instâncias padrão
        if (awayQb == null) {
            awayQb = new QbStatsDTO("QB Titular", awayTeam, "https://a.espncdn.com/combiner/i?img=/i/headshots/nfl/players/full/default.png", 0, 0, 0, 0.0, 0.0);
        }
        if (homeQb == null) {
            homeQb = new QbStatsDTO("QB Titular", homeTeam, "https://a.espncdn.com/combiner/i?img=/i/headshots/nfl/players/full/default.png", 0, 0, 0, 0.0, 0.0);
        }

        return new MatchupInsightDTO(gameId, awayTeam, homeTeam, awayQb, homeQb, injuries);
    }

    private int parseStatInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseStatDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}