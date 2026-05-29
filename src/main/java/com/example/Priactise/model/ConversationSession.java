package com.example.Priactise.model;

import com.example.Priactise.controller.Dto.PreferenceProfile;
import lombok.Data;
import java.util.*;

@Data
public class ConversationSession {
    private String sessionId;
    private PreferenceProfile preferences = new PreferenceProfile();
    private List<Map<String, String>> conversationHistory = new ArrayList<>();
    private SessionState state = SessionState.COLLECTING;
    private List<Car> shortlistedCars = new ArrayList<>();
    private int followUpRound = 0;

    public enum SessionState {
        COLLECTING, RECOMMENDED
    }

    public void addMessage(String role, String content) {
        Map<String, String> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        conversationHistory.add(msg);
    }
}
