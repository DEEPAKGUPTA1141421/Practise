package com.example.Priactise.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Car {
    private String make;
    private String model;
    private String variant;
    private double price;
    private CarSpecs specs;
    private double mileage;
    private double safetyRating;
    private List<String> userReviews;

    public String getDisplayName() {
        return make + " " + model + " " + variant;
    }

    public String toSummary() {
        String firstReview = (userReviews != null && !userReviews.isEmpty()) ? userReviews.get(0) : "N/A";
        String features = (specs.getFeatures() != null) ? String.join(", ", specs.getFeatures()) : "";
        return String.format(
            "%-40s | ₹%.2fL | %-9s | %-20s | %.1f km/l | Safety: %.1f/5 | %d seats | Boot: %s | Features: %s | Review: \"%s\"",
            getDisplayName(),
            price / 100000.0,
            specs.getFuelType(),
            specs.getTransmission(),
            mileage,
            safetyRating,
            specs.getSeatingCapacity(),
            specs.getBootSpace(),
            features,
            firstReview
        );
    }
}
