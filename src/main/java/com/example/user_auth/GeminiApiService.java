package com.example.user_auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiApiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiApiService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String geminiApiUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GeminiHistoryRepository geminiHistoryRepository;

    public GeminiApiService(GeminiHistoryRepository geminiHistoryRepository) {
        this.geminiHistoryRepository = geminiHistoryRepository;
    }

    public String getGeminiResponse(String prompt) {
        try {
            MediaType mediaType = MediaType.parse("application/json");
            String jsonPayload = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", prompt);
            RequestBody body = RequestBody.create(jsonPayload, mediaType);
            Request request = new Request.Builder()
                    .url(geminiApiUrl + "?key=" + geminiApiKey)
                    .post(body)
                    .build();

            logger.info("Sending request to Gemini API with prompt: {}", prompt);
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                logger.debug("Gemini API response: {}", responseBody);
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String geminiResponse = jsonNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();

                GeminiHistory history = new GeminiHistory();
                history.setPrompt(prompt);
                history.setResponse(geminiResponse);
                geminiHistoryRepository.save(history);

                return geminiResponse;
            } else {
                logger.error("Gemini API request failed with code: {} and message: {}", response.code(), response.message());
                throw new ResponseStatusException(
                        HttpStatus.valueOf(response.code()), "Gemini API request failed: " + response.message());
            }
        } catch (IOException e) {
            logger.error("Error communicating with Gemini API: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error communicating with Gemini API: " + e.getMessage());
        }
    }
}