package com.example.aidemo;

import jakarta.validation.constraints.NotBlank;

//public record PromptRequest(@NotBlank(message = "prompt must not be empty") String prompt) {
//}

import jakarta.validation.constraints.NotBlank;

public record PromptRequest(
        @NotBlank String prompt,
        String style  // "brief" or "detailed"
) {
}