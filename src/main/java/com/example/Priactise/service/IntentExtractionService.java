package com.example.Priactise.service;

import com.example.Priactise.controller.Dto.PreferenceProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IntentExtractionService {

    @Autowired
    private LLMService llmService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        You extract Indian car buying preferences from user messages.
        Return ONLY a valid JSON object — no markdown fences, no extra text:
        {
          "budget": <number in rupees, e.g. "15 lakhs"=1500000, or null>,
          "bodyType": <"SUV"|"Sedan"|"Hatchback"|"MPV" or null>,
          "fuelType": <"Petrol"|"Diesel"|"CNG"|"Electric"|"Hybrid" or null>,
          "transmission": <"Manual"|"Automatic" or null>,
          "usageType": <"City"|"Highway"|"Mixed" or null>,
          "priorities": <array from ["Safety","Mileage","Performance","Comfort","Resale Value","Budget"], empty [] if none>,
          "seatingCapacity": <5 or 7 or null>,
          "brandPreference": <brand name or null>
        }
        Rules:
        - "family" → add Safety + Comfort to priorities
        - "mileage"/"efficient" → add Mileage
        - "safe"/"safety" → add Safety
        - "highway" → usageType Highway, "city" → City
        - "EV"/"electric" → fuelType Electric, "auto" → Automatic
        - Budget: "L"/"lakh"/"lakhs" = ×100000. "under 15L" → 1500000
        """;

    public PreferenceProfile extractFromMessage(String message) {
        String raw = llmService.chat(SYSTEM_PROMPT, message).trim();
        // Strip markdown code fences if Gemini wraps response
        if (raw.startsWith("```")) {
            raw = raw.replaceAll("(?s)```[a-z]*\\n?", "").replace("```", "").trim();
        }
        try {
            return objectMapper.readValue(raw, PreferenceProfile.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse intent JSON from LLM: " + e.getMessage() + " | raw=" + raw, e);
        }
    }

    public PreferenceProfile merge(PreferenceProfile existing, PreferenceProfile update) {
        if (update.getBudget() != null) existing.setBudget(update.getBudget());
        if (update.getBodyType() != null && !update.getBodyType().isBlank()) existing.setBodyType(update.getBodyType());
        if (update.getFuelType() != null && !update.getFuelType().isBlank()) existing.setFuelType(update.getFuelType());
        if (update.getTransmission() != null && !update.getTransmission().isBlank()) existing.setTransmission(update.getTransmission());
        if (update.getUsageType() != null && !update.getUsageType().isBlank()) existing.setUsageType(update.getUsageType());
        if (update.getSeatingCapacity() != null) existing.setSeatingCapacity(update.getSeatingCapacity());
        if (update.getBrandPreference() != null && !update.getBrandPreference().isBlank()) existing.setBrandPreference(update.getBrandPreference());
        if (update.getPriorities() != null && update.getPriorities().length > 0) {
            Set<String> merged = new LinkedHashSet<>(existing.getPriorities() != null ? Arrays.asList(existing.getPriorities()) : List.of());
            merged.addAll(Arrays.asList(update.getPriorities()));
            existing.setPriorities(merged.toArray(new String[0]));
        }
        return existing;
    }

    public void normalizeTransmission(PreferenceProfile p) {
        if (p.getTransmission() == null) return;
        String t = p.getTransmission().toLowerCase();
        if (t.contains("auto") || t.contains("amt") || t.contains("cvt") || t.contains("dct") || t.contains("dsg") || t.contains("ivt")) {
            p.setTransmission("Automatic");
        } else if (t.contains("manual")) {
            p.setTransmission("Manual");
        }
    }
}
