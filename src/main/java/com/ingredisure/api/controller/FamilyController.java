package com.ingredisure.api.controller;

import com.ingredisure.api.model.FamilyMember;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.FamilyMemberRepository;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/family")
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001","http://localhost:3002"})
public class FamilyController {

    @Autowired private FamilyMemberRepository familyRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/members/{userId}")
    public ResponseEntity<List<Map<String,Object>>> getMembers(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        List<FamilyMember> members = familyRepo.findByUserId(userId);
        return ResponseEntity.ok(members.stream().map(this::toMap)
                .collect(Collectors.toList()));
    }

    @PostMapping("/members")
    public ResponseEntity<?> addMember(
            @RequestBody Map<String,Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.badRequest().body("User not found");

            FamilyMember member = new FamilyMember();
            member.setUser(user);
            member.setMemberName(String.valueOf(body.getOrDefault("memberName", "")));
            member.setRelationship(String.valueOf(body.getOrDefault("relationship", "")));
            member.setAge(body.get("age") != null ? Integer.parseInt(body.get("age").toString()) : null);
            member.setConditions(String.valueOf(body.getOrDefault("conditions", "")));
            member.setAvoidances(String.valueOf(body.getOrDefault("avoidances", "")));
            member.setAvatarColor(String.valueOf(body.getOrDefault("avatarColor", "#e8c49a")));

            FamilyMember saved = familyRepo.save(member);
            return ResponseEntity.ok(toMap(saved));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<?> updateMember(
            @PathVariable Long id,
            @RequestBody Map<String,Object> body,
            @RequestHeader("Authorization") String authHeader) {
        FamilyMember member = familyRepo.findById(id).orElse(null);
        if (member == null) return ResponseEntity.notFound().build();
        if (body.containsKey("memberName")) member.setMemberName(String.valueOf(body.get("memberName")));
        if (body.containsKey("relationship")) member.setRelationship(String.valueOf(body.get("relationship")));
        if (body.containsKey("age")) member.setAge(Integer.parseInt(body.get("age").toString()));
        if (body.containsKey("conditions")) member.setConditions(String.valueOf(body.get("conditions")));
        if (body.containsKey("avoidances")) member.setAvoidances(String.valueOf(body.get("avoidances")));
        familyRepo.save(member);
        return ResponseEntity.ok(toMap(member));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        if (!familyRepo.existsById(id)) return ResponseEntity.notFound().build();
        familyRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Member removed"));
    }

    private Map<String,Object> toMap(FamilyMember m) {
        Map<String,Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("memberName", m.getMemberName());
        map.put("relationship", m.getRelationship());
        map.put("age", m.getAge());
        map.put("conditions", m.getConditions());
        map.put("avoidances", m.getAvoidances());
        map.put("avatarColor", m.getAvatarColor());
        return map;
    }
}
