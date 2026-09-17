package com.java.fernando.nflpickem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public record PickRequest(
        @NotBlank(message = "O userId é obrigatório")
        String userId,

        int season,
        int weekNumber,

        @NotEmpty(message = "Você deve enviar ao menos um palpite")
        Map<String, String> picks // chave: gameId ("game-1"), valor: time vencedor ("KC")
) {}