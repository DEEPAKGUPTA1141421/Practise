package com.example.Priactise.controller.Dto;

import com.example.Priactise.model.Car;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarRecommendation {
    private Car car;
    private int rank;
    private String whyItFits;
    private String tradeoffs;
    private boolean topPick;
    private double matchScore;
}
