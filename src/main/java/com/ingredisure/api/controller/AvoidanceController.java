package com.ingredisure.api.controller;

import com.ingredisure.api.model.AvoidanceIngredient;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.AvoidanceIngredientRepository;
import com.ingredisure.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avoidances")
public class AvoidanceController {

    @Autowired
    private AvoidanceIngredientRepository avoidanceRepo;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AvoidanceIngredient>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(avoidanceRepo.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String ingredientName = body.get("ingredientName").toString();

        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        AvoidanceIngredient a = new AvoidanceIngredient();
        a.setUser(user);
        a.setIngredientName(ingredientName);
        return ResponseEntity.ok(avoidanceRepo.save(a));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        avoidanceRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}