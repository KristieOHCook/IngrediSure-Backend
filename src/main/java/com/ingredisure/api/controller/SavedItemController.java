package com.ingredisure.api.controller;

import com.ingredisure.api.model.SavedItem;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.SavedItemRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-items")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class SavedItemController {

    @Autowired private SavedItemRepository savedItemRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserItems(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        List<SavedItem> items = savedItemRepo.findByUserId(userId);
        List<Map<String, Object>> result = items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("itemName", item.getItemName());
            map.put("itemSource", item.getItemSource());
            map.put("brandOrRestaurant", item.getBrandOrRestaurant());
            map.put("ingredients", item.getIngredients());
            map.put("safetyVerdict", item.getSafetyVerdict());
            map.put("matchedTriggers", item.getMatchedTriggers());
            map.put("savedAt", item.getSavedAt() != null ? item.getSavedAt().toString() : "");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> saveItem(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            SavedItem item = new SavedItem();
            item.setUser(user);
            item.setItemName((String) body.getOrDefault("itemName", "Unknown"));
            item.setItemSource((String) body.getOrDefault("itemSource", "Grocery"));
            item.setBrandOrRestaurant((String) body.getOrDefault("brandOrRestaurant", ""));
            item.setIngredients((String) body.getOrDefault("ingredients", ""));
            item.setSafetyVerdict((String) body.getOrDefault("safetyVerdict", "Unknown"));
            item.setMatchedTriggers((String) body.getOrDefault("matchedTriggers", ""));
            item.setSavedAt(LocalDateTime.now());

            SavedItem saved = savedItemRepo.save(item);
            Map<String, Object> result = new HashMap<>();
            result.put("id", saved.getId());
            result.put("itemName", saved.getItemName());
            result.put("itemSource", saved.getItemSource());
            result.put("safetyVerdict", saved.getSafetyVerdict());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        if (!savedItemRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        savedItemRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Item removed"));
    }
}