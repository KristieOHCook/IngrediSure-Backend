package com.ingredisure.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("/api/food-search")
public class FoodSearchController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String USDA_KEY = "DEMO_KEY";

    @GetMapping
    public ResponseEntity<?> search(@RequestParam String query) {
        try {
            String url = "https://api.nal.usda.gov/fdc/v1/foods/search"
                    + "?query=" + query.toLowerCase()
                    + "&pageSize=10"
                    + "&api_key=" + USDA_KEY;

            Map result = restTemplate.getForObject(url, Map.class);

            List<Map<String, Object>> foods = (List<Map<String, Object>>) result.get("foods");
            List<Map<String, Object>> simplified = new ArrayList<>();

            if (foods != null) {
                for (Map<String, Object> food : foods) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("product_name", food.get("description"));
                    item.put("brands", food.get("brandOwner"));
                    item.put("quantity", "");

                    // Build ingredients from ingredients field
                    String ingredients = (String) food.getOrDefault("ingredients", "");
                    item.put("ingredients_text", ingredients);

                    // Get nutrients
                    List<Map> nutrients = (List<Map>) food.getOrDefault("foodNutrients", new ArrayList<>());
                    double calories = 0, sodium = 0;
                    for (Map n : nutrients) {
                        String name = String.valueOf(n.getOrDefault("nutrientName", ""));
                        Object val = n.get("value");
                        double v = val != null ? Double.parseDouble(val.toString()) : 0;
                        if (name.contains("Energy")) calories = v;
                        if (name.contains("Sodium")) sodium = v;
                    }
                    Map<String, Object> nutriments = new HashMap<>();
                    nutriments.put("energy_kcal_100g", calories);
                    nutriments.put("sodium_100g", sodium / 1000.0);
                    item.put("nutriments", nutriments);

                    simplified.add(item);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("products", simplified);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(503).body(
                    Map.of("error", "Food search unavailable: " + e.getMessage())
            );
        }
    }
}