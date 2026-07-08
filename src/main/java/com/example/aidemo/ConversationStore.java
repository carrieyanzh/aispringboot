package com.example.aidemo;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ConversationStore {

    // sessionId -> history. ConcurrentHashMap + CopyOnWriteArrayList so concurrent
    // requests for different (or the same) sessions don't corrupt state.
    private final Map<String, List<ChatTurn>> sessions = new ConcurrentHashMap<>();

    private static final int MAX_TURNS = 20; // bounds token usage sent to Gemini each call

    public List<ChatTurn> getHistory(String sessionId) {
        return sessions.getOrDefault(sessionId, List.of());
    }

    public void addTurn(String sessionId, String role, String text) {
        List<ChatTurn> history = sessions.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>());
        history.add(new ChatTurn(role, text));
        while (history.size() > MAX_TURNS) {
            history.remove(0);
        }
    }

    public void clear(String sessionId) {
        sessions.remove(sessionId);
    }
}