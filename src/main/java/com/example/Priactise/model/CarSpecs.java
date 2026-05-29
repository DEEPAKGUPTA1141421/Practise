package com.example.Priactise.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarSpecs {
    private String engine;
    private String transmission;
    private String fuelType;
    private int seatingCapacity;
    private String bootSpace;
    private List<String> features;
}
