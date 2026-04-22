package com.ingredisure.api.controller;

import com.ingredisure.api.model.User;
import com.ingredisure.api.model.UserAvoidance;
import com.ingredisure.api.repository.AvoidanceRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/avoidances")
@CrossOrigin(origins = "http://localhost:3000")
public class AvoidanceController {

    @Autowired private AvoidanceRepository avoidanceRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    private User resolveUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7).trim();
        if (!jwtUtil.isValid(token)) return null;
        String username = jwtUtil.extractUsername(token);
        return userRepo.findByUsername(username).orElse(null);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAvoidances(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long userId) {
        User user = resolveUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(avoidanceRepo.findByUser(user));
    }

    @GetMapping
    public ResponseEntity<?> getAvoidancesNoId(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = resolveUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(avoidanceRepo.findByUser(user));
    }

    @PostMapping
    public ResponseEntity<?> addAvoidance(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body) {
        User user = resolveUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String ingredientName = (String) body.get("ingredientName");
        if (ingredientName == null || ingredientName.isBlank())
            return ResponseEntity.badRequest().body("ingredientName is required");
        UserAvoidance avoidance = new UserAvoidance();
        avoidance.setUser(user);
        avoidance.setIngredientName(ingredientName.trim());
        return ResponseEntity.ok(avoidanceRepo.save(avoidance));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAvoidance(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        User user = resolveUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        avoidanceRepo.findById(id).ifPresent(a -> {
            if (a.getUser().getId().equals(user.getId())) avoidanceRepo.deleteById(id);
        });
        return ResponseEntity.ok().build();
    }
}