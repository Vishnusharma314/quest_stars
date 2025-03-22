package com.example.user_auth.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.user_auth.repository.GeminiHistoryRepository;
import com.example.user_auth.model.GeminiHistory;
import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
@Service
public class GeminiApiService {
   private static final Logger logger = LoggerFactory.getLogger(GeminiApiService.class);
   @Value("${gemini.api.key}")
   private String geminiApiKey;
   @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
   private String geminiApiUrl;
   // Modified client initialization
   private final OkHttpClient client = createUnsafeClient();
   private final ObjectMapper objectMapper = new ObjectMapper();
   private final GeminiHistoryRepository geminiHistoryRepository;
   public GeminiApiService(GeminiHistoryRepository geminiHistoryRepository) {
       this.geminiHistoryRepository = geminiHistoryRepository;
   }
   // Create unsafe client with SSL bypass
   private OkHttpClient createUnsafeClient() {
       try {
           final TrustManager[] trustAllCerts = new TrustManager[]{
               new X509TrustManager() {
                   @Override
                   public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                   @Override
                   public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                   @Override
                   public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                       return new java.security.cert.X509Certificate[]{};
                   }
               }
           };
           final SSLContext sslContext = SSLContext.getInstance("SSL");
           sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
           return new OkHttpClient.Builder()
               .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
               .hostnameVerifier((hostname, session) -> true)
               .connectTimeout(60, TimeUnit.SECONDS)
               .readTimeout(60, TimeUnit.SECONDS)
               .writeTimeout(60, TimeUnit.SECONDS)
               .build();
       } catch (Exception e) {
           throw new RuntimeException("Failed to create unsafe OkHttpClient", e);
       }
   }
   public String getGeminiResponse(String prompt, Long userId) {
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
               logger.info("User Id: {}", userId); 
               history.setUserId(userId);
               LocalDateTime now = LocalDateTime.now();
               history.setDateTime(now);
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