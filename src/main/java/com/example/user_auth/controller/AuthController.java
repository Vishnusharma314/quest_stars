package com.example.user_auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.example.user_auth.model.User;
import com.example.user_auth.model.UserToken;
import com.example.user_auth.service.AuthService;
import com.example.user_auth.service.GeminiApiService;
import com.example.user_auth.service.TokenUtils;
import com.example.user_auth.dto.GeminiRequest;
import com.example.user_auth.dto.UserRequest;
import com.example.user_auth.dto.ApiResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import com.example.user_auth.repository.UserTokenRepository;
import org.springframework.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private UserTokenRepository userTokenRepository;
    private final AuthService authService;
    private final GeminiApiService geminiApiService;
    private final TokenUtils tokenUtils;


    public AuthController(AuthService authService, GeminiApiService geminiApiService, UserTokenRepository userTokenRepository, TokenUtils tokenUtils) {
        this.authService = authService;
        this.geminiApiService = geminiApiService;
        this.userTokenRepository = userTokenRepository;
        this.tokenUtils = tokenUtils;
    }

    // Common token validation method
    private Long verifyToken(String token) {
        return tokenUtils.isTokenValid(token);
    }

    @PostMapping("/gemini")
    public ResponseEntity<Object> getGeminiResponse(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody GeminiRequest request) {
        String token = authorizationHeader.replace("Bearer ", "");  // Remove the "Bearer " prefix
        Long userId = verifyToken(token);
        if (userId != null) {
            // Call your service and return the response as a String within ResponseEntity
            String response = geminiApiService.getGeminiResponse(request.getPrompt(), userId);
            return ResponseEntity.ok(response);  // Return a successful response with status 200
        } else {
            // Error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Unauthorized User.");

            return ResponseEntity.status(401).body(errorResponse);  // Return conflict status with the error message
        }
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
        String token = generateToken(guestUser.getUsername());
        guestInfo.put("token", token);
        // Save the token in the database
        saveToken(guestUser.getId(), token);
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
            String token = generateToken(user.get().getUsername());
            userInfo.put("token", token);

            // Save the token in the database
            saveToken(user.get().getId(), token);
            

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

    private void saveToken(Long userId, String token) {
        UserToken userToken = new UserToken();
        userToken.setUserId(userId);
        userToken.setToken(token);
        userToken.setExpirationDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); // 1 hour expiration
        userTokenRepository.save(userToken); // Assuming you have a repository for UserToken
    }
}

