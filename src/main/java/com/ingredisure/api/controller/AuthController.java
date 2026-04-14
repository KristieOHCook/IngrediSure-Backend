package com.ingredisure.api.controller;

import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        
        if (userRepo.existsByUsername(username)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username already taken"));
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        User saved = userRepo.save(user);

        String token = jwtUtil.generateToken(saved.getUsername(), saved.getId(), saved.getRole());
  
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("token", token, "userId", saved.getId(),
                        "username", saved.getUsername(), "role", saved.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        return userRepo.findByUsername(username)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getUsername(), u.getId(), u.getRole());
  
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "userId", u.getId(),
                            "username", u.getUsername(),
                            "role", u.getRole()
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials")));
    }
}