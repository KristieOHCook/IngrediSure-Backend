package com.ingredisure.api.controller;

import com.ingredisure.api.model.SavedItem;
import com.ingredisure.api.repository.SavedItemRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-items")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class SavedItemController {

    @Autowired private SavedItemRepository savedItemRepo;
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
            map.put("savedAt", item.getSavedAt() != null ?
                    item.getSavedAt().toString() : "");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
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