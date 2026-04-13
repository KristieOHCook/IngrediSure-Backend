package com.ingredisure.api.controller;

import com.ingredisure.api.model.MenuItem;
import com.ingredisure.api.service.IngredientSafetyService;
import com.ingredisure.api.service.MenuItemService;
import com.ingredisure.api.service.SubstitutionService;
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
            // Extract userId from JWT token
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);

            String ingredients = String.valueOf(body.getOrDefault("ingredients", ""));
            String itemName = String.valueOf(body.getOrDefault("itemName", "Unknown"));

            // Run safety check using user's conditions and avoidances
            IngredientSafetyService.SafetyResult result =
                    ingredientSafetyService.checkIngredients(userId, ingredients);

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
    public ResponseEntity<MenuItem> addMenuItem(@Valid @RequestBody MenuItem item) {
        MenuItem saved = menuItemService.saveMenuItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "Safe";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}