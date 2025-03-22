package com.example.user_auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.example.user_auth.model.User;
import com.example.user_auth.service.AuthService;
import com.example.user_auth.service.GeminiApiService;
import com.example.user_auth.dto.GeminiRequest;
import com.example.user_auth.dto.UserRequest;
import com.example.user_auth.dto.ApiResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GeminiApiService geminiApiService;


    public AuthController(AuthService authService, GeminiApiService geminiApiService) {
        this.authService = authService;
        this.geminiApiService = geminiApiService;
    }


    @PostMapping("/gemini")
    public String getGeminiResponse(@RequestBody GeminiRequest request) {
        return geminiApiService.getGeminiResponse(request.getPrompt());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserRequest request) {
        try {
            Map<String, Object> userInfo = authService.registerUser(request.getUsername(), request.getPassword(), "ROLE_USER");

            // Return as a JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully!");
            response.put("data", userInfo);

            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(409).body(errorResponse);
        }
    }


    @PostMapping("/guest-login")
    public ResponseEntity<ApiResponse> guestLogin() {
        User guestUser = authService.createGuestUser();

        Map<String, Object> guestInfo = new HashMap<>();
        guestInfo.put("id", guestUser.getId());
        guestInfo.put("username", guestUser.getUsername());
        guestInfo.put("role", guestUser.getRole());

        return ResponseEntity
                .status(200)
                .body(new ApiResponse(true, "Guest login successful!", guestInfo));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody UserRequest request) {
        Optional<User> user = authService.findByUsername(request.getUsername());

        if (user.isPresent() && authService.checkPassword(request.getPassword(), user.get().getPassword())) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.get().getId());
            userInfo.put("username", user.get().getUsername());
            userInfo.put("role", user.get().getRole());
            userInfo.put("token", generateToken(user.get().getUsername()));
            

            return ResponseEntity
                    .status(200)
                    .body(new ApiResponse(true, "Login successful!", userInfo));
        } else {
            return ResponseEntity
                    .status(401)
                    .body(new ApiResponse(false, "Invalid username or password"));
        }
    }

    private String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .compact();
    }
}

