package com.example.aidemo;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank String prompt,
        @NotBlank String sessionId,
        String style
) {}