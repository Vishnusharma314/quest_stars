package com.example.user_auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@CrossOrigin(origins = "*")
public class GeminiApiController {

    private final GeminiApiService geminiApiService;

    @Autowired
    public GeminiApiController(GeminiApiService geminiApiService) {
        this.geminiApiService = geminiApiService;
    }

    @PostMapping("/gemini")
    public String getGeminiResponse(@RequestBody GeminiRequest request) {
        return geminiApiService.getGeminiResponse(request.getPrompt());
    }
}