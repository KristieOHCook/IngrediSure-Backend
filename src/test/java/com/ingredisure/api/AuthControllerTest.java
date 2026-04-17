package com.ingredisure.api;

import com.ingredisure.api.controller.AuthController;
import com.ingredisure.api.model.User;
import com.ingredisure.api.repository.UserRepository;
import com.ingredisure.api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("$2a$10$hashedpassword");
        testUser.setRole("ROLE_USER");
    }

    @Test
    void register_Success_ReturnsCreated() {
        when(userRepo.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedpassword");
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("mock.jwt.token");

        Map<String, String> body = Map.of(
                "username", "testuser",
                "email", "test@test.com",
                "password", "password123"
        );

        ResponseEntity<?> response = authController.register(body);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ReturnsBadRequest() {
        when(userRepo.existsByUsername("testuser")).thenReturn(true);

        Map<String, String> body = Map.of(
                "username", "testuser",
                "email", "test@test.com",
                "password", "password123"
        );

        ResponseEntity<?> response = authController.register(body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void register_PasswordIsHashed_NeverStoredPlainText() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("plainpassword")).thenReturn("$2a$10$hashed");
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("token");

        Map<String, String> body = Map.of(
                "username", "newuser",
                "email", "new@test.com",
                "password", "plainpassword"
        );

        authController.register(body);
        verify(passwordEncoder, times(1)).encode("plainpassword");
    }

    @Test
    void login_ValidCredentials_ReturnsOkWithToken() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("mock.jwt.token");

        Map<String, String> body = Map.of(
                "username", "testuser",
                "password", "password123"
        );

        ResponseEntity<?> response = authController.login(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals("mock.jwt.token", responseBody.get("token"));
    }

    @Test
    void login_UserNotFound_ReturnsUnauthorized() {
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        Map<String, String> body = Map.of(
                "username", "unknown",
                "password", "password123"
        );

        ResponseEntity<?> response = authController.login(body);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_WrongPassword_ReturnsUnauthorized() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        Map<String, String> body = Map.of(
                "username", "testuser",
                "password", "wrongpassword"
        );

        ResponseEntity<?> response = authController.login(body);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void login_AdminUser_ReturnsAdminRole() {
        testUser.setRole("ROLE_ADMIN");
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), eq("ROLE_ADMIN"))).thenReturn("admin.token");

        Map<String, String> body = Map.of(
                "username", "admin",
                "password", "password"
        );

        ResponseEntity<?> response = authController.login(body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("ROLE_ADMIN", responseBody.get("role"));
    }
}