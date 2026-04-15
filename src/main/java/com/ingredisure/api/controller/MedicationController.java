package com.ingredisure.api.controller;

import com.ingredisure.api.model.Medication;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.MedicationRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medications")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class MedicationController {

    @Autowired private MedicationRepository medicationRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserMedications(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        List<Medication> meds = medicationRepo.findByUserId(userId);
        List<Map<String, Object>> result = meds.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("medicationName", m.getMedicationName());
            map.put("dosage", m.getDosage());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> addMedication(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.badRequest().body("User not found");

            Medication med = new Medication();
            med.setUser(user);
            med.setMedicationName(body.get("medicationName"));
            med.setDosage(body.getOrDefault("dosage", ""));
            medicationRepo.save(med);
            return ResponseEntity.ok(Map.of("message", "Medication added"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedication(@PathVariable Long id) {
        if (!medicationRepo.existsById(id))
            return ResponseEntity.notFound().build();
        medicationRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Medication removed"));
    }
}
