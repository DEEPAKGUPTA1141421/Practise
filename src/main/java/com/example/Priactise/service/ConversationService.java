package com.example.Priactise.service;

import com.example.Priactise.controller.Dto.*;
import com.example.Priactise.model.Car;
import com.example.Priactise.model.ConversationSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    @Autowired
    private IntentExtractionService intentExtraction;
    @Autowired
    private CarFilteringService carFiltering;
    @Autowired
    private RecommendationService recommendation;
    @Autowired
    private CarDataService carData;

    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();

    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = (request.getSessionId() == null || request.getSessionId().isBlank())
                ? UUID.randomUUID().toString()
                : request.getSessionId();

        ConversationSession session = sessions.computeIfAbsent(sessionId, id -> {
            ConversationSession s = new ConversationSession();
            s.setSessionId(id);
            return s;
        });

        String msg = request.getMessage().trim();
        session.addMessage("user", msg);

        // Allow restart after recommendation
        if (session.getState() == ConversationSession.SessionState.RECOMMENDED) {
            if (msg.toLowerCase().contains("new search") || msg.toLowerCase().contains("start over")
                    || msg.toLowerCase().contains("restart")) {
                ConversationSession fresh = new ConversationSession();
                fresh.setSessionId(sessionId);
                sessions.put(sessionId, fresh);
                session = fresh;
            } else {
                return buildResponse(session, "done",
                        "I've shown my recommendations above. Type \"new search\" to start over.", null);
            }
        }

        // Extract intent and merge into session preferences
        PreferenceProfile extracted = intentExtraction.extractFromMessage(msg);
        intentExtraction.normalizeTransmission(extracted);
        intentExtraction.merge(session.getPreferences(), extracted);

        List<String> missing = getMissingFields(session.getPreferences());
        boolean readyToRecommend = missing.isEmpty() || session.getFollowUpRound() >= 2;

        if (!readyToRecommend) {
            return askFollowUp(session, missing);
        }
        return generateRecommendations(session);
    }

    private List<String> getMissingFields(PreferenceProfile p) {
        List<String> missing = new ArrayList<>();
        if (p.getBudget() == null)
            missing.add("budget");
        if (p.getFuelType() == null)
            missing.add("fuelType");
        if (p.getTransmission() == null)
            missing.add("transmission");
        return missing;
    }

    private ChatResponse askFollowUp(ConversationSession session, List<String> missing) {
        session.setFollowUpRound(session.getFollowUpRound() + 1);
        PreferenceProfile p = session.getPreferences();

        StringBuilder sb = new StringBuilder();

        // Contextual acknowledgment
        boolean hasContext = p.getBudget() != null || p.getBodyType() != null || p.getFuelType() != null;
        if (hasContext) {
            sb.append("Got it");
            if (p.getBodyType() != null)
                sb.append(" — a ").append(p.getBodyType());
            if (p.getBudget() != null)
                sb.append(" under ₹").append(String.format("%.0f", p.getBudget() / 100000)).append("L");
            if (p.getPriorities() != null && p.getPriorities().length > 0)
                sb.append(", prioritizing ").append(String.join(" & ", p.getPriorities()));
            sb.append("!\n\nJust ").append(missing.size() == 1 ? "one more question" : "a couple more things")
                    .append(":\n");
        } else {
            sb.append("Happy to help you find the right car!\n\nA few quick questions:\n");
        }

        // Ask max 2 questions per round
        int qNum = 1;
        for (String field : missing.stream().limit(2).toList()) {
            sb.append(qNum++).append(". ");
            switch (field) {
                case "budget" -> sb.append("What's your budget? (e.g., ₹8L, ₹12L, ₹20L)\n");
                case "fuelType" -> sb.append("Fuel preference — Petrol, Diesel, CNG, or Electric?\n");
                case "transmission" -> sb.append("Manual or Automatic?\n");
            }
        }

        String text = sb.toString().trim();
        session.addMessage("assistant", text);
        return buildResponse(session, "followup", text, null);
    }

    private ChatResponse generateRecommendations(ConversationSession session) {
        session.setState(ConversationSession.SessionState.RECOMMENDED);

        List<Car> filtered = carFiltering.filter(session.getPreferences(), carData.getAllCars());
        session.setShortlistedCars(filtered);

        if (filtered.isEmpty()) {
            String noResultMsg = buildNoResultMessage(session.getPreferences());
            session.addMessage("assistant", noResultMsg);
            // Allow them to try again
            session.setState(ConversationSession.SessionState.COLLECTING);
            session.setFollowUpRound(0);
            return buildResponse(session, "no_results", noResultMsg, null);
        }

        List<CarRecommendation> recs = recommendation.recommend(session.getPreferences(), filtered);

        String summary = buildSummaryMessage(session.getPreferences(), filtered.size(), recs.size());
        session.addMessage("assistant", summary);
        return buildResponse(session, "recommendation", summary, recs);
    }

    private String buildSummaryMessage(PreferenceProfile p, int total, int recCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(total).append(" matching cars");
        if (p.getBudget() != null)
            sb.append(" under ₹").append(String.format("%.0f", p.getBudget() / 100000)).append("L");
        sb.append(". Here are my top ").append(recCount).append(" picks for you:");
        return sb.toString();
    }

    private String buildNoResultMessage(PreferenceProfile p) {
        StringBuilder sb = new StringBuilder("No cars matched all your criteria");
        if (p.getBudget() != null)
            sb.append(" (budget: ₹").append(String.format("%.0f", p.getBudget() / 100000)).append("L)");
        sb.append(
                ". Try relaxing your filters — a wider budget, different fuel type, or removing the transmission preference. What would you like to adjust?");
        return sb.toString();
    }

    private ChatResponse buildResponse(ConversationSession session, String type, String message,
            List<CarRecommendation> recs) {
        PreferenceProfile p = session.getPreferences();
        return ChatResponse.builder()
                .sessionId(session.getSessionId())
                .message(message)
                .responseType(type)
                .recommendations(recs)
                .preferences(ChatResponse.PreferenceSnapshot.builder()
                        .budget(p.getBudget())
                        .bodyType(p.getBodyType())
                        .fuelType(p.getFuelType())
                        .transmission(p.getTransmission())
                        .usageType(p.getUsageType())
                        .priorities(p.getPriorities())
                        .seatingCapacity(p.getSeatingCapacity())
                        .build())
                .build();
    }
}
