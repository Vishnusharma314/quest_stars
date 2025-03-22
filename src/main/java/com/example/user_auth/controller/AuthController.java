package com.example.user_auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.example.user_auth.model.User;
import com.example.user_auth.service.AuthService;
import com.example.user_auth.dto.ApiResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestParam String username, @RequestParam String password) {
        try {
            Map<String, Object> userInfo = authService.registerUser(username, password, "ROLE_USER");

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
    public ResponseEntity<ApiResponse> loginUser(@RequestParam String username, @RequestParam String password) {
        Optional<User> user = authService.findByUsername(username);

        if (user.isPresent() && authService.checkPassword(password, user.get().getPassword())) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.get().getId());
            userInfo.put("username", user.get().getUsername());
            userInfo.put("role", user.get().getRole());

            return ResponseEntity
                    .status(200)
                    .body(new ApiResponse(true, "Login successful!", userInfo));
        } else {
            return ResponseEntity
                    .status(401)
                    .body(new ApiResponse(false, "Invalid username or password"));
        }
    }
}

