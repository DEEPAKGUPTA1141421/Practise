package com.example.Priactise.controller.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreferenceProfile {
    private Double budget;
    private String bodyType;
    private String fuelType;
    private String transmission;
    private String[] priorities;
    private String usageType;
    private Integer seatingCapacity;
    private String brandPreference;

    public boolean isComplete() {
        return budget != null
                && isFilled(fuelType)
                && isFilled(transmission);
    }

    private boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
