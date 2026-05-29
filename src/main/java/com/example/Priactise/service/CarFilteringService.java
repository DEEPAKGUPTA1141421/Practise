package com.example.Priactise.service;

import com.example.Priactise.controller.Dto.PreferenceProfile;
import com.example.Priactise.model.Car;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarFilteringService {

    private static final Map<String, String> BODY_TYPE_MAP = Map.ofEntries(
        // SUVs
        Map.entry("Seltos", "SUV"),
        Map.entry("XUV300", "SUV"),
        Map.entry("Urban Cruiser", "SUV"),
        Map.entry("Urban Cruiser Hyryder", "SUV"),
        Map.entry("EcoSport", "SUV"),
        Map.entry("Taigun", "SUV"),
        Map.entry("Kiger", "SUV"),
        Map.entry("Magnite", "SUV"),
        Map.entry("Hector", "SUV"),
        Map.entry("Venue", "SUV"),
        Map.entry("Nexon", "SUV"),
        Map.entry("Brezza", "SUV"),
        Map.entry("Scorpio N", "SUV"),
        Map.entry("Punch", "SUV"),
        Map.entry("Sonet", "SUV"),
        Map.entry("Creta", "SUV"),
        Map.entry("Fortuner", "SUV"),
        Map.entry("XUV700", "SUV"),
        Map.entry("Kodiaq", "SUV"),
        Map.entry("Alcazar", "SUV"),
        Map.entry("WR-V", "SUV"),
        Map.entry("Kushaq", "SUV"),
        Map.entry("Kicks", "SUV"),
        Map.entry("Astor", "SUV"),
        Map.entry("Harrier", "SUV"),
        Map.entry("Thar", "SUV"),
        Map.entry("Tiguan Allspace", "SUV"),
        Map.entry("Q3", "SUV"),
        Map.entry("Range Rover Evoque", "SUV"),
        // Hatchbacks
        Map.entry("Swift", "Hatchback"),
        Map.entry("i20", "Hatchback"),
        Map.entry("Tiago", "Hatchback"),
        Map.entry("Baleno", "Hatchback"),
        Map.entry("Grand i10 NIOS", "Hatchback"),
        Map.entry("Altroz", "Hatchback"),
        Map.entry("i20 N Line", "Hatchback"),
        Map.entry("Glanza", "Hatchback"),
        // Sedans
        Map.entry("City", "Sedan"),
        Map.entry("Slavia", "Sedan"),
        Map.entry("Aura", "Sedan"),
        Map.entry("Verna", "Sedan"),
        Map.entry("Amaze", "Sedan"),
        Map.entry("Yaris", "Sedan"),
        Map.entry("Virtus", "Sedan"),
        Map.entry("Dzire", "Sedan"),
        Map.entry("Civic", "Sedan"),
        Map.entry("A4", "Sedan"),
        Map.entry("2 Series Gran Coupe", "Sedan"),
        Map.entry("A-Class Limousine", "Sedan"),
        Map.entry("Superb", "Sedan"),
        // MPVs
        Map.entry("Triber", "MPV"),
        Map.entry("GO+", "MPV"),
        Map.entry("Ertiga", "MPV"),
        Map.entry("Innova Crysta", "MPV"),
        Map.entry("Carnival", "MPV"),
        Map.entry("Carens", "MPV")
    );

    public String getBodyType(Car car) {
        return BODY_TYPE_MAP.getOrDefault(car.getModel(), "Unknown");
    }

    public List<Car> filter(PreferenceProfile profile, List<Car> allCars) {
        List<Car> result = new ArrayList<>(allCars);

        // Budget — allow 10% over to catch "just over budget" cars
        if (profile.getBudget() != null) {
            double max = profile.getBudget() * 1.10;
            result = result.stream()
                .filter(c -> c.getPrice() <= max)
                .collect(Collectors.toList());
        }

        // Body type
        if (profile.getBodyType() != null && !profile.getBodyType().isBlank()) {
            String target = profile.getBodyType().toLowerCase();
            result = result.stream()
                .filter(c -> getBodyType(c).toLowerCase().contains(target))
                .collect(Collectors.toList());
        }

        // Fuel type
        if (profile.getFuelType() != null && !profile.getFuelType().isBlank()) {
            String target = profile.getFuelType().toLowerCase();
            result = result.stream()
                .filter(c -> c.getSpecs().getFuelType().toLowerCase().contains(target))
                .collect(Collectors.toList());
        }

        // Transmission — "Automatic" means exclude manual
        if (profile.getTransmission() != null && !profile.getTransmission().isBlank()) {
            String target = profile.getTransmission().toLowerCase();
            if (target.equals("automatic")) {
                result = result.stream()
                    .filter(c -> !c.getSpecs().getTransmission().toLowerCase().contains("manual"))
                    .collect(Collectors.toList());
            } else {
                result = result.stream()
                    .filter(c -> c.getSpecs().getTransmission().toLowerCase().contains("manual"))
                    .collect(Collectors.toList());
            }
        }

        // Minimum seating capacity
        if (profile.getSeatingCapacity() != null) {
            result = result.stream()
                .filter(c -> c.getSpecs().getSeatingCapacity() >= profile.getSeatingCapacity())
                .collect(Collectors.toList());
        }

        // Brand preference (soft filter — prefer, don't exclude)
        if (profile.getBrandPreference() != null && !profile.getBrandPreference().isBlank()) {
            String brand = profile.getBrandPreference().toLowerCase();
            List<Car> preferred = result.stream()
                .filter(c -> c.getMake().toLowerCase().contains(brand))
                .collect(Collectors.toList());
            if (!preferred.isEmpty()) {
                List<Car> rest = result.stream()
                    .filter(c -> !c.getMake().toLowerCase().contains(brand))
                    .collect(Collectors.toList());
                result = new ArrayList<>(preferred);
                result.addAll(rest);
            }
        }

        result = rankByPriorities(result, profile.getPriorities());
        return result.stream().limit(15).collect(Collectors.toList());
    }

    private List<Car> rankByPriorities(List<Car> cars, String[] priorities) {
        Set<String> prioSet = priorities != null
            ? Arrays.stream(priorities).map(String::toLowerCase).collect(Collectors.toSet())
            : Set.of();

        boolean safety = prioSet.contains("safety");
        boolean mileage = prioSet.contains("mileage");
        boolean performance = prioSet.contains("performance");
        boolean budget = prioSet.contains("budget");
        boolean resale = prioSet.contains("resale value");

        return cars.stream()
            .sorted(Comparator.comparingDouble(
                (Car c) -> score(c, safety, mileage, performance, budget, resale)
            ).reversed())
            .collect(Collectors.toList());
    }

    private double score(Car car, boolean safety, boolean mileage, boolean perf, boolean budget, boolean resale) {
        double s = car.getSafetyRating() * 4; // baseline safety always matters
        if (safety)  s += car.getSafetyRating() * 16;
        if (mileage) s += car.getMileage() * 2;
        if (perf)    s += car.getPrice() / 200_000.0;
        if (budget)  s += Math.max(0, 30 - car.getPrice() / 100_000.0);
        if (resale) {
            String make = car.getMake().toLowerCase();
            if (make.contains("maruti") || make.contains("suzuki")) s += 10;
            else if (make.contains("hyundai") || make.contains("toyota"))  s += 8;
            else if (make.contains("honda") || make.contains("tata"))      s += 5;
        }
        return s;
    }
}
