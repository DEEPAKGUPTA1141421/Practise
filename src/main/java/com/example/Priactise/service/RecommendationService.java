package com.example.Priactise.service;

import com.example.Priactise.controller.Dto.CarRecommendation;
import com.example.Priactise.controller.Dto.PreferenceProfile;
import com.example.Priactise.model.Car;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    @Autowired
    private LLMService llmService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        You are an expert car buying advisor for the Indian market.
        Given a user's preference profile and a list of matching cars, recommend the best %d DISTINCT cars.

        IMPORTANT: Every "carIndex" in your response MUST be a different number. Never repeat the same carIndex.
        If there are fewer cars than requested, return only as many as are available.

        Return ONLY a valid JSON array — no markdown fences, no extra text:
        [
          {
            "rank": 1,
            "carIndex": <0-based index from the car list>,
            "whyItFits": "<2-3 sentences citing actual specs: price, mileage, safety rating, key features>",
            "tradeoffs": "<1-2 sentences on genuine downsides>",
            "topPick": true,
            "matchScore": <float 0-10>
          }
        ]
        rank-1 must have topPick=true, others false. Be specific and honest. Reference actual numbers.
        """;

    public List<CarRecommendation> recommend(PreferenceProfile prefs, List<Car> cars) {
        int count = Math.min(3, cars.size());
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, count);
        String userContent = buildUserContent(prefs, cars, count);
        String raw = llmService.chat(systemPrompt, userContent).trim();

        if (raw.startsWith("```")) {
            raw = raw.replaceAll("(?s)```[a-z]*\\n?", "").replace("```", "").trim();
        }

        try {
            JsonNode arr = objectMapper.readTree(raw);
            List<CarRecommendation> recs = new ArrayList<>();
            Set<Integer> seenIndexes = new HashSet<>();

            for (JsonNode node : arr) {
                int idx = node.get("carIndex").asInt();
                // Skip out-of-bounds and duplicate carIndexes
                if (idx < 0 || idx >= cars.size() || seenIndexes.contains(idx)) continue;
                seenIndexes.add(idx);
                recs.add(CarRecommendation.builder()
                    .car(cars.get(idx))
                    .rank(node.get("rank").asInt())
                    .whyItFits(node.get("whyItFits").asText())
                    .tradeoffs(node.get("tradeoffs").asText())
                    .topPick(node.get("topPick").asBoolean())
                    .matchScore(node.get("matchScore").asDouble())
                    .build());
            }
            recs.sort(Comparator.comparingInt(CarRecommendation::getRank));
            return recs;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse recommendation JSON from LLM: " + e.getMessage() + " | raw=" + raw, e);
        }
    }

    private String buildUserContent(PreferenceProfile prefs, List<Car> cars, int count) {
        StringBuilder sb = new StringBuilder("USER PREFERENCES:\n");
        if (prefs.getBudget() != null)       sb.append("- Budget: ₹").append(String.format("%.0f", prefs.getBudget() / 100000)).append(" Lakhs\n");
        if (prefs.getBodyType() != null)     sb.append("- Body type: ").append(prefs.getBodyType()).append("\n");
        if (prefs.getFuelType() != null)     sb.append("- Fuel: ").append(prefs.getFuelType()).append("\n");
        if (prefs.getTransmission() != null) sb.append("- Transmission: ").append(prefs.getTransmission()).append("\n");
        if (prefs.getUsageType() != null)    sb.append("- Usage: ").append(prefs.getUsageType()).append("\n");
        if (prefs.getPriorities() != null && prefs.getPriorities().length > 0)
            sb.append("- Priorities: ").append(String.join(", ", prefs.getPriorities())).append("\n");
        if (prefs.getSeatingCapacity() != null)
            sb.append("- Min seats: ").append(prefs.getSeatingCapacity()).append("\n");

        sb.append("\nMATCHING CARS (use 0-based index):\n");
        for (int i = 0; i < Math.min(cars.size(), 15); i++) {
            sb.append(i).append(". ").append(cars.get(i).toSummary()).append("\n");
        }
        sb.append(String.format("\nPick the best %d DISTINCT cars (different carIndex each) for this buyer.", count));
        return sb.toString();
    }
}
