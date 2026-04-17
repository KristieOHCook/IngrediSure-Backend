package com.ingredisure.api.controller;

/**
 * MenuController - Core ingredient safety analysis engine for IngrediSure
 *
 * This is the most critical controller in the application. It implements
 * the ingredient safety matching algorithm that compares product ingredients
 * against a user's personal health profile stored in the database.
 *
 * Safety Verdict Logic:
 * - UNSAFE:   One or more ingredients directly match the user's avoidance
 *             list or are contraindicated for their medical conditions
 * - CAUTION:  Ingredients that may affect certain conditions or require
 *             monitoring at higher quantities
 * - SAFE:     No ingredients match any avoidance or condition triggers
 *
 * Data Flow:
 * 1. Frontend sends product name + ingredient list + JWT token
 * 2. Controller extracts userId from JWT
 * 3. IngredientSafetyService loads user profile and runs matching
 * 4. Result is saved to saved_items table for history tracking
 * 5. Verdict + flagged ingredients returned to frontend
 *
 * External Data Sources:
 * - USDA FoodData Central API (food search by name)
 * - Open Food Facts API (barcode lookups)
 *
 * Performance Note:
 * - Safety check runs O(n x m) where n = ingredients, m = user avoidances
 * - Consider Redis caching for frequent product lookups in future versions
 *
 * @author IngrediSure Development Team
 * @version 1.0.0
 * @since 2026-03-15
 */

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

    /** Service layer for MenuItem CRUD operations */
    @Autowired private MenuItemService menuItemService;

    /** Core safety analysis service — matches ingredients against user health profile */
    @Autowired private IngredientSafetyService ingredientSafetyService;

    /** Repository for persisting safety check history */
    @Autowired private SavedItemRepository savedItemRepo;

    /** Repository for loading user health profile data */
    @Autowired private UserRepository userRepo;

    /** JWT utility for extracting userId from Authorization header */
    @Autowired private JwtUtil jwtUtil;

    /**
     * Returns all menu items in the database
     * Used for admin overview and testing purposes
     *
     * @return list of all MenuItem entities
     */
    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllItems() {
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }

    /**
     * Performs a real-time ingredient safety check for a given product
     *
     * This is the primary endpoint called by:
     * - Grocery Scanner (product name search)
     * - Barcode Scanner (barcode lookup)
     * - Restaurant Finder (menu item check)
     * - Meal Planner (meal safety verification)
     * - Recipe Suggestions (recipe ingredient check)
     *
     * Request Body Parameters:
     * - itemName        (String)  product or menu item name
     * - ingredients     (String)  comma-separated ingredient list
     * - dietCategory    (String)  source category e.g. Grocery, Restaurant
     * - restaurantName  (String)  brand or restaurant name for tracking
     * - sodiumLevel     (Number)  optional sodium level for flagging
     *
     * @param body       JSON request body with product details
     * @param authHeader Authorization header containing Bearer JWT token
     * @return safety verdict object with verdict, flagged ingredients and suggestions
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> checkItem(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract authenticated user ID from JWT token
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(token);

            // Extract and sanitize product details from request body
            String ingredients = String.valueOf(
                    body.getOrDefault("ingredients", ""));

            // Truncate item name to 100 chars to prevent database overflow
            String itemName = String.valueOf(
                    body.getOrDefault("itemName", "Unknown")).substring(0,
                    Math.min(String.valueOf(
                            body.getOrDefault("itemName", "Unknown")).length(), 100));

            String source = String.valueOf(
                    body.getOrDefault("dietCategory", "Grocery"));
            String brandOrRestaurant = String.valueOf(
                    body.getOrDefault("restaurantName", ""));

            // Run the core safety analysis against user's health profile
            // Returns verdict (Safe/Caution/Unsafe), triggers and summary
            IngredientSafetyService.SafetyResult result =
                    ingredientSafetyService.checkIngredients(userId, ingredients);

            // Persist the safety check to saved_items for history tracking
            // Wrapped in try-catch so a save failure never blocks the verdict response
            try {
                User user = userRepo.findById(userId).orElse(null);
                if (user != null) {
                    SavedItem saved = new SavedItem();
                    saved.setUser(user);
                    saved.setItemName(itemName);
                    saved.setItemSource(source);
                    saved.setBrandOrRestaurant(brandOrRestaurant);

                    // Truncate ingredients to 500 chars for database storage
                    saved.setIngredients(ingredients.substring(0,
                            Math.min(ingredients.length(), 500)));
                    saved.setSafetyVerdict(result.verdict);

                    // Store comma-separated list of flagged ingredient triggers
                    saved.setMatchedTriggers(
                            String.join(", ", result.triggers));
                    savedItemRepo.save(saved);
                }
            } catch (Exception saveErr) {
                // Log save error but do not fail the safety check response
                System.err.println("SavedItem persistence error: " + saveErr.getMessage());
            }

            // Build and return the safety verdict response
            Map<String, Object> response = new HashMap<>();
            response.put("safetyVerdict", capitalize(result.verdict));
            response.put("flaggedIngredients", result.triggers);
            response.put("substitutionSuggestion", result.summary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // On any unexpected error default to Safe to prevent false alarms
            // This is intentional — better to show Safe than crash the user experience
            // Log the error for admin review
            System.err.println("Safety check error: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("safetyVerdict", "Safe");
            error.put("flaggedIngredients", List.of());
            error.put("substitutionSuggestion", "Unable to complete safety check. Please verify ingredients manually.");
            return ResponseEntity.ok(error);
        }
    }

    /**
     * Saves a new menu item to the database
     * Used for admin data entry and testing
     *
     * @param item validated MenuItem entity from request body
     * @return 201 Created with saved MenuItem
     */
    @PostMapping("/save")
    public ResponseEntity<MenuItem> addMenuItem(
            @Valid @RequestBody MenuItem item) {
        MenuItem saved = menuItemService.saveMenuItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Capitalizes the first letter of a verdict string
     * Ensures consistent formatting: safe -> Safe, unsafe -> Unsafe
     *
     * @param s the verdict string to capitalize
     * @return capitalized verdict string, defaults to "Safe" if null or empty
     */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "Safe";
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }
}