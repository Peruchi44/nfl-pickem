package com.java.fernando.nflpickem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "O apelido não pode estar em branco")
        @Size(min = 2, max = 20, message = "O apelido deve ter entre 2 e 20 caracteres")
        String username
) {}