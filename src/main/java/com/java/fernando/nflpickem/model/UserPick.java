package com.java.fernando.nflpickem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "picks")
@CompoundIndex(name = "user_week_idx", def = "{'userId': 1, 'weekNumber': 1, 'season': 1}", unique = true)
public class UserPick {

    @Id
    private String id;
    private String userId;
    private int season;
    private int weekNumber;

    // Chave: gameId | Valor: time escolhido (sigla/nome)
    private Map<String, String> picks = new HashMap<>();

    // Margem de vitória palpitada para o jogo tiebreaker
    private Integer tiebreakerMargin;

    private int totalPoints = 0;
    private Instant updatedAt = Instant.now();

    public UserPick() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }

    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }

    public Map<String, String> getPicks() { return picks; }
    public void setPicks(Map<String, String> picks) { this.picks = picks; }

    public Integer getTiebreakerMargin() { return tiebreakerMargin; }
    public void setTiebreakerMargin(Integer tiebreakerMargin) { this.tiebreakerMargin = tiebreakerMargin; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}