package com.example.aidemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .build();

//    public String askGemini(String userPrompt) {
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(
//                        Map.of("parts", List.of(Map.of("text", userPrompt)))
//                )
//        );
//
//        try {
//            Map<String, Object> response = webClient.post()
//                    .uri("/models/gemini-2.5-flash:generateContent?key=" + apiKey)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .timeout(Duration.ofSeconds(15))
//                    .block();
//
//            return extractText(response);
//
//        } catch (WebClientResponseException e) {
//            log.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new RuntimeException("AI service is currently unavailable");
//        } catch (Exception e) {
//            log.error("Unexpected error calling Gemini", e);
//            throw new RuntimeException("Failed to process request");
//        }
//    }

    public String askGemini(String userPrompt) {

        // Option A: instruction added to the prompt text
       // String instruction = "Answer concisely in 3-4 sentences unless the user asks for more detail.\n\nQuestion: ";


        String instruction = """
    Answer clearly and completely. Use short paragraphs or bullet points.
    Keep the response focused — long enough to fully answer the question,
    but avoid unnecessary repetition or filler.

    Question:
    """;
        String fullPrompt = instruction + userPrompt;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", fullPrompt)))
                ),
                // Option B: hard cap on output length
                "generationConfig", Map.of(
                        //"maxOutputTokens", 200,
                        "maxOutputTokens", 800,
                        "temperature", 0.7
                )
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            return extractText(response);

        } catch (WebClientResponseException e) {
            log.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI service is currently unavailable");
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini", e);
            throw new RuntimeException("Failed to process request");
        }
    }

    public String askGeminiWithHistory(List<ChatTurn> history, String userPrompt, String style) {

        String styleInstruction = switch (style == null ? "" : style.toLowerCase()) {
            case "brief" -> "Answer in 1-2 concise sentences.\n\n";
            case "detailed" -> "Answer thoroughly, with examples where useful.\n\n";
            default -> "Answer clearly and completely, avoiding unnecessary repetition.\n\n";
        };

        List<Map<String, Object>> contents = new ArrayList<>();

        for (ChatTurn turn : history) {
            contents.add(Map.of(
                    "role", turn.role(),                      // "user" or "model"
                    "parts", List.of(Map.of("text", turn.text()))
            ));
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", styleInstruction + userPrompt))
        ));

        Map<String, Object> requestBody = Map.of(
                "contents", contents,
                "generationConfig", Map.of(
                        "maxOutputTokens", 800,
                        "temperature", 0.7
                )
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            return extractText(response);

        } catch (WebClientResponseException e) {
            log.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI service is currently unavailable");
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini", e);
            throw new RuntimeException("Failed to process request");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> body) {
        var candidates = (List<Map<String, Object>>) body.get("candidates");
        var content = (Map<String, Object>) candidates.get(0).get("content");
        var parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }
}