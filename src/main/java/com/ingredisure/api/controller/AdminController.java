package com.ingredisure.api.controller;

import com.ingredisure.api.model.SavedItem;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001","http://localhost:3002"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository userRepo;
    @Autowired private SavedItemRepository savedItemRepo;
    @Autowired private FeedbackRepository feedbackRepo;
    @Autowired private MedicationRepository medicationRepo;
    @Autowired private NutritionLogRepository nutritionRepo;
    @Autowired private GroceryListRepository groceryListRepo;
    @Autowired private FamilyMemberRepository familyMemberRepo;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String,Object>>> getAllUsers() {
        List<User> users = userRepo.findAll();
        List<Map<String,Object>> result = users.stream().map(u -> {
            Map<String,Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail() != null ? u.getEmail() : "");
            map.put("role", u.getRole());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable Long id) {
        return userRepo.findById(id).map(u -> {
            u.setRole("ROLE_ADMIN");
            userRepo.save(u);
            return ResponseEntity.ok(Map.of("message", u.getUsername() + " promoted to ROLE_ADMIN"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        return userRepo.findById(id).map(u -> {
            if ("ROLE_ADMIN".equals(u.getRole()))
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot deactivate an admin"));
            u.setRole("ROLE_BANNED");
            userRepo.save(u);
            return ResponseEntity.ok(Map.of("message", u.getUsername() + " deactivated"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String,Object>> getAnalytics() {
        Map<String,Object> analytics = new HashMap<>();

        List<User> allUsers = userRepo.findAll();
        List<SavedItem> allItems = savedItemRepo.findAll();

        // User stats
        long totalUsers = allUsers.size();
        long adminUsers = allUsers.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();
        long bannedUsers = allUsers.stream().filter(u -> "ROLE_BANNED".equals(u.getRole())).count();
        long regularUsers = totalUsers - adminUsers - bannedUsers;

        analytics.put("totalUsers", totalUsers);
        analytics.put("adminUsers", adminUsers);
        analytics.put("bannedUsers", bannedUsers);
        analytics.put("regularUsers", regularUsers);

        // Safety check stats
        long totalSavedItems = allItems.size();
        long safeItems = allItems.stream().filter(i -> "Safe".equalsIgnoreCase(i.getSafetyVerdict())).count();
        long cautionItems = allItems.stream().filter(i -> "Caution".equalsIgnoreCase(i.getSafetyVerdict())).count();
        long unsafeItems = allItems.stream().filter(i -> "Unsafe".equalsIgnoreCase(i.getSafetyVerdict())).count();

        analytics.put("totalSavedItems", totalSavedItems);
        analytics.put("safeItems", safeItems);
        analytics.put("cautionItems", cautionItems);
        analytics.put("unsafeItems", unsafeItems);

        // Feature usage
        analytics.put("totalMedications", medicationRepo.count());
        analytics.put("totalNutritionLogs", nutritionRepo.count());
        analytics.put("totalGroceryLists", groceryListRepo.count());
        analytics.put("totalFamilyMembers", familyMemberRepo.count());
        analytics.put("totalFeedback", feedbackRepo.count());

        // Top flagged ingredients
        Map<String,Long> ingredientCounts = new HashMap<>();
        allItems.stream()
                .filter(i -> i.getMatchedTriggers() != null && !i.getMatchedTriggers().isEmpty())
                .forEach(i -> {
                    for (String trigger : i.getMatchedTriggers().split(",")) {
                        String t = trigger.trim().toLowerCase();
                        if (!t.isEmpty()) ingredientCounts.merge(t, 1L, Long::sum);
                    }
                });
        List<Map<String,Object>> topIngredients = ingredientCounts.entrySet().stream()
                .sorted(Map.Entry.<String,Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> { Map<String,Object> m = new HashMap<>(); m.put("ingredient", e.getKey()); m.put("count", e.getValue()); return m; })
                .collect(Collectors.toList());
        analytics.put("topFlaggedIngredients", topIngredients);

        // Top item sources
        Map<String,Long> sourceCounts = allItems.stream()
                .filter(i -> i.getItemSource() != null)
                .collect(Collectors.groupingBy(i -> i.getItemSource(), Collectors.counting()));
        analytics.put("itemSources", sourceCounts);

        // Safety breakdown percentage
        if (totalSavedItems > 0) {
            analytics.put("safePercent", Math.round((safeItems * 100.0) / totalSavedItems));
            analytics.put("cautionPercent", Math.round((cautionItems * 100.0) / totalSavedItems));
            analytics.put("unsafePercent", Math.round((unsafeItems * 100.0) / totalSavedItems));
        } else {
            analytics.put("safePercent", 0);
            analytics.put("cautionPercent", 0);
            analytics.put("unsafePercent", 0);
        }

        // System info
        analytics.put("serverTime", LocalDateTime.now().toString());
        analytics.put("javaVersion", System.getProperty("java.version"));
        analytics.put("appVersion", "1.0.0");

        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/saved-items")
    public ResponseEntity<List<SavedItem>> getAllSavedItems() {
        return ResponseEntity.ok(savedItemRepo.findAll());
    }

    @DeleteMapping("/feedback/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        if (!feedbackRepo.existsById(id)) return ResponseEntity.notFound().build();
        feedbackRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Feedback deleted"));
    }
}