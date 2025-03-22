package com.example.user_auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.user_auth.repository.GeminiHistoryRepository;
import com.example.user_auth.model.GeminiHistory;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.example.user_auth.service.TokenUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.ResponseEntity;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class GeminiHistoryController {

    private final GeminiHistoryRepository geminiHistoryRepository;
    private final TokenUtils tokenUtils;

    @Autowired
    public GeminiHistoryController(GeminiHistoryRepository geminiHistoryRepository, TokenUtils tokenUtils) {
        this.geminiHistoryRepository = geminiHistoryRepository;
        this.tokenUtils = tokenUtils;
    }

    // Common token validation method
    private Long verifyToken(String token) {
        return tokenUtils.isTokenValid(token);
    }

    @GetMapping("/history")
    public ResponseEntity<Object> getAllHistory(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");  // Remove the "Bearer " prefix
        Long userId = verifyToken(token);
        
        if (userId != null) {
            List<GeminiHistory> historyList = geminiHistoryRepository.findByUserId(userId);
            
            // Return the response with status 200 and the list of histories
            return ResponseEntity.ok(historyList);
        } else {
            // If the token is invalid, return an error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Unauthorized User.");
            
            return ResponseEntity.status(401).body(errorResponse);  // Return 401 Unauthorized with the error message
        }
    }

}