package com.java.fernando.nflpickem.model;

import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;

public class Game {

    @Field("_id")
    private String id;

    private String homeTeam;
    private String awayTeam;
    private String homeLogo;
    private String awayLogo;
    private Instant kickOff;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private String winnerTeam;

    public Game() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public String getHomeLogo() { return homeLogo; }
    public void setHomeLogo(String homeLogo) { this.homeLogo = homeLogo; }

    public String getAwayLogo() { return awayLogo; }
    public void setAwayLogo(String awayLogo) { this.awayLogo = awayLogo; }

    public Instant getKickOff() { return kickOff; }
    public void setKickOff(Instant kickOff) { this.kickOff = kickOff; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }

    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }

    public String getWinnerTeam() { return winnerTeam; }
    public void setWinnerTeam(String winnerTeam) { this.winnerTeam = winnerTeam; }
}