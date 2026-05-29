package com.example.Priactise.service;

import com.example.Priactise.model.Car;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.List;

@Service
public class CarDataService {

    private List<Car> cars;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadData() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cars-india-dataset.json")) {
            if (is == null) throw new RuntimeException("cars-india-dataset.json not found in classpath");
            cars = objectMapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load car dataset: " + e.getMessage(), e);
        }
    }

    public List<Car> getAllCars() {
        return cars;
    }
}
