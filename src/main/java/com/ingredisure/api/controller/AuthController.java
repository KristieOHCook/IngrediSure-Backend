package com.ingredisure.api.controller;

/**
 * AuthController - Handles all authentication operations for IngrediSure
 *
 * Responsibilities:
 * - User registration with BCrypt password hashing
 * - User login with JWT token generation
 * - Role-based access control (ROLE_USER, ROLE_ADMIN)
 *
 * Security considerations:
 * - Passwords are never stored in plain text
 * - JWT tokens expire after 24 hours (86400000ms)
 * - CORS restricted to localhost:3000, 3001, 3002
 *
 * @author IngrediSure Development Team
 * @version 1.0.0
 * @since 2026-03-15
 */

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

    /**
     * Registers a new user account
     *
     * Validation:
     * - Username must be unique (enforced at DB level with UNIQUE constraint)
     * - Password is hashed using BCrypt with strength factor 10
     * - Phone number and communication preferences are optional
     *
     * @param body JSON body containing username, email, password, phone, emailOptIn, smsOptIn
     * @return 201 Created with JWT token, or 400 Bad Request if username taken
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        // Check for duplicate username before creating account
        if (userRepo.existsByUsername(username)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username already taken"));
        }

        // Build new user with encoded password and default role
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");

        // Optional fields — gracefully handle if not provided
        try { user.setPhone(body.getOrDefault("phone", "")); } catch (Exception ignored) {}
        try { user.setEmailOptIn(true); } catch (Exception ignored) {}
        try { user.setSmsOptIn(false); } catch (Exception ignored) {}

        User saved = userRepo.save(user);

        // Generate JWT token including userId and role for frontend authorization
        String token = jwtUtil.generateToken(saved.getUsername(), saved.getId(), saved.getRole());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "token", token,
                        "userId", saved.getId(),
                        "username", saved.getUsername(),
                        "role", saved.getRole()
                ));
    }

    /**
     * Authenticates an existing user and returns a JWT token
     *
     * Authentication flow:
     * 1. Look up user by username
     * 2. Verify password using BCrypt matches()
     * 3. Generate JWT token with 24hr expiry
     * 4. Return token with user metadata for frontend storage
     *
     * @param body JSON body containing username and password
     * @return 200 OK with JWT token, or 401 Unauthorized if credentials invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");

            // Find user by username — returns Optional
            var userOpt = userRepo.findByUsername(username);

            // Return 401 if user not found
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }

            User u = userOpt.get();

            // Verify password using BCrypt — never compare plain text
            if (!passwordEncoder.matches(password, u.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }

            // Generate signed JWT token with 24hr expiry
            String token = jwtUtil.generateToken(u.getUsername(), u.getId(), u.getRole());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", u.getId(),
                    "username", u.getUsername(),
                    "role", u.getRole()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}