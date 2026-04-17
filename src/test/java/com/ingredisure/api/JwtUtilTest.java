package com.ingredisure.api;

import com.ingredisure.api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "ingredisure-secret-key-must-be-at-least-32-characters-long";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
    }

    @Test
    void generateToken_ValidInputs_ReturnsNonNullToken() {
        String token = jwtUtil.generateToken("testuser", 1L, "ROLE_USER");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_ContainsThreeParts_JwtFormat() {
        String token = jwtUtil.generateToken("testuser", 1L, "ROLE_USER");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void extractUsername_ValidToken_ReturnsCorrectUsername() {
        String token = jwtUtil.generateToken("testuser", 1L, "ROLE_USER");
        assertEquals("testuser", jwtUtil.extractUsername(token));
    }

    @Test
    void extractUserId_ValidToken_ReturnsCorrectUserId() {
        String token = jwtUtil.generateToken("testuser", 42L, "ROLE_USER");
        assertEquals(42L, jwtUtil.extractUserId(token));
    }

    @Test
    void extractRole_AdminToken_ReturnsAdminRole() {
        String token = jwtUtil.generateToken("admin", 1L, "ROLE_ADMIN");
        assertEquals("ROLE_ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void extractRole_UserToken_ReturnsUserRole() {
        String token = jwtUtil.generateToken("user", 1L, "ROLE_USER");
        assertEquals("ROLE_USER", jwtUtil.extractRole(token));
    }

    @Test
    void isValid_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken("testuser", 1L, "ROLE_USER");
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void isValid_InvalidToken_ReturnsFalse() {
        assertFalse(jwtUtil.isValid("invalid.token.here"));
    }

    @Test
    void isValid_EmptyToken_ReturnsFalse() {
        assertFalse(jwtUtil.isValid(""));
    }

    @Test
    void isValid_TamperedToken_ReturnsFalse() {
        String token = jwtUtil.generateToken("testuser", 1L, "ROLE_USER");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtUtil.isValid(tampered));
    }

    @Test
    void generateToken_DifferentUsers_ProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken("user1", 1L, "ROLE_USER");
        String token2 = jwtUtil.generateToken("user2", 2L, "ROLE_USER");
        assertNotEquals(token1, token2);
    }
}
