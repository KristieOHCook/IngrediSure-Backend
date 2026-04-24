package com.ingredisure.api.controller;

import com.ingredisure.api.model.User;
import com.ingredisure.api.model.UserCondition;
import com.ingredisure.api.repository.ConditionRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conditions")
@CrossOrigin(origins = "http://localhost:3000")
public class ConditionController {

    @Autowired private ConditionRepository conditionRepo;
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
    public ResponseEntity<?> getConditions(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long userId) {
        User user = resolveUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(conditionRepo.findByUser(user));
    }

    @GetMapping
    public ResponseEntity<?> getConditionsNoId(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        User user = resolveUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(conditionRepo.findByUser(user));
    }

    @PostMapping
    public ResponseEntity<?> addCondition(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body) {
        User user = resolveUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String conditionName = (String) body.get("conditionName");
        if (conditionName == null || conditionName.isBlank())
            return ResponseEntity.badRequest().body("conditionName is required");
        UserCondition condition = new UserCondition();
        condition.setUser(user);
        condition.setConditionName(conditionName.trim());
        return ResponseEntity.ok(conditionRepo.save(condition));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCondition(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        User user = resolveUser(auth);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        conditionRepo.findById(id).ifPresent(c -> {
            if (c.getUser().getId().equals(user.getId())) conditionRepo.deleteById(id);
        });
        return ResponseEntity.ok().build();
    }
}