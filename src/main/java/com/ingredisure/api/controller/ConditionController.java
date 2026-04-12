package com.ingredisure.api.controller;

import com.ingredisure.api.model.MedicalCondition;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.MedicalConditionRepository;
import com.ingredisure.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conditions")
public class ConditionController {

    @Autowired
    private MedicalConditionRepository conditionRepo;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MedicalCondition>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(conditionRepo.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String conditionName = body.get("conditionName").toString();
        String severity = body.getOrDefault("severity", "moderate").toString();

        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        MedicalCondition c = new MedicalCondition();
        c.setUser(user);
        c.setConditionName(conditionName);
        c.setSeverity(severity);
        return ResponseEntity.ok(conditionRepo.save(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        conditionRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
