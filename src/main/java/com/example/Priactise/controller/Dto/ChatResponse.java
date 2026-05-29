package com.example.Priactise.controller.Dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChatResponse {
    private String sessionId;
    private String message;
    private String responseType; // "followup", "recommendation", "no_results", "error"
    private List<CarRecommendation> recommendations;
    private PreferenceSnapshot preferences;

    @Data
    @Builder
    public static class PreferenceSnapshot {
        private Double budget;
        private String bodyType;
        private String fuelType;
        private String transmission;
        private String usageType;
        private String[] priorities;
        private Integer seatingCapacity;
    }
}
