package com.ingredisure.api.controller;

import com.ingredisure.api.model.MenuItem;
import com.ingredisure.api.model.SavedItem;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.MenuItemRepository;
import com.ingredisure.api.repository.SavedItemRepository;
import com.ingredisure.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository userRepo;
    @Autowired private MenuItemRepository menuRepo;
    @Autowired private SavedItemRepository savedItemRepo;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepo.findAll();
        users.forEach(u -> u.setPassword("[HIDDEN]"));
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(u -> {
                    u.setRole("ROLE_ADMIN");
                    userRepo.save(u);
                    return ResponseEntity.ok(Map.of("message",
                            "User " + u.getUsername() + " promoted to ROLE_ADMIN"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(u -> {
                    if ("ROLE_ADMIN".equals(u.getRole())) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Cannot deactivate an admin"));
                    }
                    u.setRole("ROLE_BANNED");
                    userRepo.save(u);
                    return ResponseEntity.ok(Map.of("message",
                            u.getUsername() + " has been deactivated"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        if (!menuRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        menuRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Menu item " + id + " deleted"));
    }

    @GetMapping("/saved-items")
    public ResponseEntity<List<SavedItem>> getAllSavedItems() {
        return ResponseEntity.ok(savedItemRepo.findAll());
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalUsers", userRepo.count());
        analytics.put("totalMenuItems", menuRepo.count());
        analytics.put("totalSavedItems", savedItemRepo.count());
        analytics.put("safetyBreakdown", Map.of(
                "safe", savedItemRepo.countBySafetyVerdict("safe"),
                "caution", savedItemRepo.countBySafetyVerdict("caution"),
                "unsafe", savedItemRepo.countBySafetyVerdict("unsafe")
        ));
        return ResponseEntity.ok(analytics);
    }
}