package com.ingredisure.api.controller;

import com.ingredisure.api.model.MenuItem;
import com.ingredisure.api.model.SavedItem;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.SavedItemRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.service.IngredientSafetyService;
import com.ingredisure.api.service.MenuItemService;
import com.ingredisure.api.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class MenuController {

    @Autowired private MenuItemService menuItemService;
    @Autowired private IngredientSafetyService ingredientSafetyService;
    @Autowired private SavedItemRepository savedItemRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllItems() {
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> checkItem(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract user from JWT
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(token);

            String ingredients = String.valueOf(
                    body.getOrDefault("ingredients", ""));
            String itemName = String.valueOf(
                    body.getOrDefault("itemName", "Unknown")).substring(0,
                    Math.min(String.valueOf(body.getOrDefault("itemName", "Unknown")).length(), 100));
            String source = String.valueOf(
                    body.getOrDefault("dietCategory", "Grocery"));
            String brandOrRestaurant = String.valueOf(
                    body.getOrDefault("restaurantName", ""));

            // Run safety check
            IngredientSafetyService.SafetyResult result =
                    ingredientSafetyService.checkIngredients(userId, ingredients);

            // Save the check to saved_items
            try {
                User user = userRepo.findById(userId).orElse(null);
                if (user != null) {
                    SavedItem saved = new SavedItem();
                    saved.setUser(user);
                    saved.setItemName(itemName);
                    saved.setItemSource(source);
                    saved.setBrandOrRestaurant(brandOrRestaurant);
                    saved.setIngredients(ingredients.substring(0,
                            Math.min(ingredients.length(), 500)));
                    saved.setSafetyVerdict(result.verdict);
                    saved.setMatchedTriggers(
                            String.join(", ", result.triggers));
                    savedItemRepo.save(saved);
                }
            } catch (Exception saveErr) {
                // Don't fail the request if save fails
                System.err.println("Save error: " + saveErr.getMessage());
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("safetyVerdict", capitalize(result.verdict));
            response.put("flaggedIngredients", result.triggers);
            response.put("substitutionSuggestion", result.summary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("safetyVerdict", "Safe");
            error.put("flaggedIngredients", List.of());
            error.put("substitutionSuggestion", "No issues detected.");
            return ResponseEntity.ok(error);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<MenuItem> addMenuItem(
            @Valid @RequestBody MenuItem item) {
        MenuItem saved = menuItemService.saveMenuItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "Safe";
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }
}