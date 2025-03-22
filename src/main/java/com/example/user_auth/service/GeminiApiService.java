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
import com.example.user_auth.model.ArticleQuestion;
import com.example.user_auth.repository.ArticleQuestionRepository;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

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

    @Autowired
    private ArticleQuestionRepository articleQuestionRepository;

   public GeminiApiService(GeminiHistoryRepository geminiHistoryRepository, ArticleQuestionRepository articleQuestionRepository) {
       this.geminiHistoryRepository = geminiHistoryRepository;
       this.articleQuestionRepository = articleQuestionRepository;
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
    String appendedPrompt = prompt + " and respond strictly in JSON format with only these params report_summary, therapeutic_implications, therapy_response_prediction (with numeric in these child params: parp_inhibitors_sensitivity,platinum_based_chemotherapy_sensitivity, immunotherapy_response). Do not include any other untold child params";
       System.out.println("Prompt: " + appendedPrompt);
       logger.info("Full prompt being sent to Gemini API: {}", appendedPrompt);
       try {
           MediaType mediaType = MediaType.parse("application/json");
           String jsonPayload = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", appendedPrompt);
           RequestBody body = RequestBody.create(jsonPayload, mediaType);
           Request.Builder requestBuilder = new Request.Builder()
                   .url(geminiApiUrl + "?key=" + geminiApiKey)
                   .post(body);
           Request request = requestBuilder.build();
           logger.info("Sending request to Gemini API with prompt: {}", prompt);
           Response response = client.newCall(request).execute();
           if (response.isSuccessful()) {
               String responseBody = response.body().string();
               logger.debug("Gemini API response: {}", responseBody);
               // Trim "json" and backticks from the beginning and end of the response
               responseBody = responseBody.trim();
               if (responseBody.startsWith("json")) {
                   responseBody = responseBody.substring(4).trim();
               }
               responseBody = responseBody.replace("`", "");
               responseBody = responseBody.replace("json", "");
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
    public String processArticleAndQuestion(Long userId, String pubmedArticle, String userQuestion) {
        String fullPrompt = "Based on the following article: " + pubmedArticle + " and answer the question: " + userQuestion + " in short. Respond strictly in JSON format with only single paramster as ai_answer";
        logger.info("Full prompt being sent to Gemini API: {}", fullPrompt);
        try {
            MediaType mediaType = MediaType.parse("application/json");
            String jsonPayload = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", fullPrompt);
            RequestBody body = RequestBody.create(jsonPayload, mediaType);
            Request request = new Request.Builder()
                    .url(geminiApiUrl + "?key=" + geminiApiKey)
                    .post(body)
                    .build();
            logger.info("Sending request to Gemini API with prompt: {}", fullPrompt);
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                logger.debug("Gemini API response: {}", responseBody);
                // Trim "json" and backticks from the beginning and end of the response
                responseBody = responseBody.trim();
                if (responseBody.startsWith("json")) {
                    responseBody = responseBody.substring(4).trim();
                }
                responseBody = responseBody.replace("`", "");
                responseBody = responseBody.replace("json", "");
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String geminiResponse = jsonNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
                ArticleQuestion articleQuestion = new ArticleQuestion();
                articleQuestion.setPubmedArticle(pubmedArticle);
                articleQuestion.setUserQuestion(userQuestion);
                articleQuestion.setUserId(userId);
                articleQuestion.setCreatedAt(LocalDateTime.now());
                articleQuestionRepository.save(articleQuestion);
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

    public List<ArticleQuestion> getArticleQuestionHistory(Long userId) {
        return articleQuestionRepository.findByUserId(userId);
    }
}