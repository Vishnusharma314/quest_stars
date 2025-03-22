package com.example.user_auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class GeminiHistoryController {

    private final GeminiHistoryRepository geminiHistoryRepository;

    @Autowired
    public GeminiHistoryController(GeminiHistoryRepository geminiHistoryRepository) {
        this.geminiHistoryRepository = geminiHistoryRepository;
    }

    @GetMapping("/history")
    public List<GeminiHistory> getAllHistory() {
        return geminiHistoryRepository.findAll();
    }
}