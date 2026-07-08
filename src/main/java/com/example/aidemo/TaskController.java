package com.example.aidemo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final GeminiService geminiService;

    public TaskController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/summarize")
    public ResponseEntity<Map<String, String>> summarize(@Valid @RequestBody TextRequest request) {
        String prompt = "Summarize the following text in 3 bullet points:\n\n" + request.text();
        return ResponseEntity.ok(Map.of("response", geminiService.askGemini(prompt)));
    }

    @PostMapping("/translate")
    public ResponseEntity<Map<String, String>> translate(@Valid @RequestBody TranslateRequest request) {
        String prompt = "Translate the following text to " + request.targetLanguage() + ":\n\n" + request.text();
        return ResponseEntity.ok(Map.of("response", geminiService.askGemini(prompt)));
    }

    public record TextRequest(@NotBlank String text) {}
    public record TranslateRequest(@NotBlank String text, @NotBlank String targetLanguage) {}
}