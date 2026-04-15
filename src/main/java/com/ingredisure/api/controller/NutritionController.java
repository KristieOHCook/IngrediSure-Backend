package com.ingredisure.api.controller;

import com.ingredisure.api.model.NutritionLog;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.NutritionLogRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nutrition")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class NutritionController {

    @Autowired private NutritionLogRepository nutritionRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/today/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getTodayLogs(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        List<NutritionLog> logs = nutritionRepo
                .findByUserIdAndLogDate(userId, LocalDate.now());
        return ResponseEntity.ok(buildResponse(logs));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        List<NutritionLog> logs = nutritionRepo
                .findByUserIdOrderByLogDateDesc(userId);
        return ResponseEntity.ok(buildResponse(logs));
    }

    @PostMapping
    public ResponseEntity<?> logMeal(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.badRequest()
                    .body("User not found");

            NutritionLog log = new NutritionLog();
            log.setUser(user);
            log.setMealName(String.valueOf(
                    body.getOrDefault("mealName", "Meal")));
            log.setMealType(String.valueOf(
                    body.getOrDefault("mealType", "Snack")));
            log.setCalories(Integer.parseInt(
                    body.getOrDefault("calories", "0").toString()));
            log.setProtein(Double.parseDouble(
                    body.getOrDefault("protein", "0").toString()));
            log.setCarbs(Double.parseDouble(
                    body.getOrDefault("carbs", "0").toString()));
            log.setFat(Double.parseDouble(
                    body.getOrDefault("fat", "0").toString()));
            log.setSodium(Double.parseDouble(
                    body.getOrDefault("sodium", "0").toString()));
            log.setFiber(Double.parseDouble(
                    body.getOrDefault("fiber", "0").toString()));

            nutritionRepo.save(log);
            return ResponseEntity.ok(Map.of("message", "Meal logged"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLog(@PathVariable Long id) {
        if (!nutritionRepo.existsById(id))
            return ResponseEntity.notFound().build();
        nutritionRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Log deleted"));
    }

    private List<Map<String, Object>> buildResponse(
            List<NutritionLog> logs) {
        return logs.stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("mealName", log.getMealName());
            map.put("mealType", log.getMealType());
            map.put("calories", log.getCalories());
            map.put("protein", log.getProtein());
            map.put("carbs", log.getCarbs());
            map.put("fat", log.getFat());
            map.put("sodium", log.getSodium());
            map.put("fiber", log.getFiber());
            map.put("logDate", log.getLogDate().toString());
            return map;
        }).collect(Collectors.toList());
    }
}