package com.java.fernando.nflpickem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "weeks")
public class Week {

    @Id
    private String id;
    private int season;
    private int weekNumber;
    private String label;
    private boolean current;
    private List<Game> games = new ArrayList<>();

    // <-- ADICIONE AQUI O ATRIBUTO DO PASSO 2
    private String tiebreakerGameId;

    public Week() {}

    // ... Seus getters e setters existentes (getId, getSeason, getGames, etc.) ...

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getSeason() { return season; }
    public void setSeason(int season) { this.season = season; }

    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }

    public List<Game> getGames() { return games; }
    public void setGames(List<Game> games) { this.games = games; }

    // <-- ADICIONE AQUI OS GETTERS E SETTERS DO PASSO 2
    public String getTiebreakerGameId() {
        return tiebreakerGameId;
    }

    public void setTiebreakerGameId(String tiebreakerGameId) {
        this.tiebreakerGameId = tiebreakerGameId;
    }
}