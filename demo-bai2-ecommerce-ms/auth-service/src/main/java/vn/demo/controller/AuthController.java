package vn.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.demo.dto.request.LoginRequest;
import vn.demo.dto.request.RefreshTokenRequest;
import vn.demo.dto.request.RegisterRequest;
import vn.demo.dto.response.LoginResponse;
import vn.demo.dto.response.RegisterResponse;
import vn.demo.service.AuthService;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * CONTROLLER — Auth API (public). Login trả accessToken.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user account
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        
        // 1) Process registration through service layer
        RegisterResponse response = authService.register(request);
        
        // 2) Return success response
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user and generate JWT tokens
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        
        // 1) Authenticate user and generate tokens
        LoginResponse response = authService.login(request);
        
        // 2) Return tokens and user info
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        
        // 1) Generate fresh tokens using refresh token
        LoginResponse response = authService.refreshToken(request);
        
        // 2) Return new tokens
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user by revoking refresh token
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request received");
        
        // 1) Revoke refresh token
        authService.logout(request.getRefreshToken());
        
        // 2) Return success message
        return ResponseEntity.ok(Map.of(
            "message", "Logout successful"
        ));
    }
}