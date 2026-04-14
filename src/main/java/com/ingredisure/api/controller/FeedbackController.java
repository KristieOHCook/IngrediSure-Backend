package com.ingredisure.api.controller;

import com.ingredisure.api.model.Feedback;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.FeedbackRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class FeedbackController {

    @Autowired private FeedbackRepository feedbackRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.badRequest().body("User not found");

            Feedback feedback = new Feedback();
            feedback.setUser(user);
            feedback.setRating(Integer.parseInt(
                    body.getOrDefault("rating", "5").toString()));
            feedback.setLiked(String.valueOf(
                    body.getOrDefault("liked", "")));
            feedback.setDisliked(String.valueOf(
                    body.getOrDefault("disliked", "")));
            feedback.setSuggestion(String.valueOf(
                    body.getOrDefault("suggestion", "")));

            feedbackRepo.save(feedback);
            return ResponseEntity.ok(Map.of("message", "Thank you for your feedback!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    "Error saving feedback: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllFeedback() {
        List<Feedback> all = feedbackRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Feedback f : all) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", f.getId());
            item.put("username", f.getUser() != null ? f.getUser().getUsername() : "Anonymous");
            item.put("rating", f.getRating());
            item.put("liked", f.getLiked());
            item.put("disliked", f.getDisliked());
            item.put("suggestion", f.getSuggestion());
            item.put("submittedAt", f.getSubmittedAt() != null ? f.getSubmittedAt().toString() : "");
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        if (!feedbackRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        feedbackRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<Feedback> all = feedbackRepo.findAll();
        double avgRating = all.stream()
                .mapToInt(Feedback::getRating)
                .average().orElse(0.0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalReviews", all.size());
        summary.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
        summary.put("fiveStars", all.stream().filter(f -> f.getRating() == 5).count());
        summary.put("fourStars", all.stream().filter(f -> f.getRating() == 4).count());
        summary.put("threeStars", all.stream().filter(f -> f.getRating() == 3).count());
        summary.put("twoStars", all.stream().filter(f -> f.getRating() == 2).count());
        summary.put("oneStar", all.stream().filter(f -> f.getRating() == 1).count());
        return ResponseEntity.ok(summary);
    }
}