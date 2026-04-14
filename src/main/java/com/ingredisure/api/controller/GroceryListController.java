package com.ingredisure.api.controller;

import com.ingredisure.api.model.GroceryList;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.GroceryListRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grocery-lists")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
public class GroceryListController {

    @Autowired private GroceryListRepository groceryListRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserLists(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        List<GroceryList> lists =
                groceryListRepo.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = lists.stream().map(list -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", list.getId());
            map.put("listName", list.getListName());
            map.put("recipeName", list.getRecipeName());
            map.put("items", list.getItems());
            map.put("createdAt", list.getCreatedAt() != null ?
                    list.getCreatedAt().toString() : "");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createList(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.badRequest()
                    .body("User not found");

            GroceryList list = new GroceryList();
            list.setUser(user);
            list.setListName(String.valueOf(
                    body.getOrDefault("listName", "My Grocery List")));
            list.setRecipeName(String.valueOf(
                    body.getOrDefault("recipeName", "")));
            list.setItems(String.valueOf(
                    body.getOrDefault("items", "")));

            GroceryList saved = groceryListRepo.save(list);
            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("listName", saved.getListName());
            response.put("recipeName", saved.getRecipeName());
            response.put("items", saved.getItems());
            response.put("createdAt", saved.getCreatedAt().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error creating list: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateList(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            GroceryList list = groceryListRepo.findById(id).orElse(null);
            if (list == null) return ResponseEntity.notFound().build();

            if (body.containsKey("listName"))
                list.setListName(String.valueOf(body.get("listName")));
            if (body.containsKey("items"))
                list.setItems(String.valueOf(body.get("items")));

            groceryListRepo.save(list);
            return ResponseEntity.ok(Map.of("message", "List updated"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error updating list: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteList(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        if (!groceryListRepo.existsById(id))
            return ResponseEntity.notFound().build();
        groceryListRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "List deleted"));
    }
}
