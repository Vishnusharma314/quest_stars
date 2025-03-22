package com.example.user_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserAuthApplication.class, args);
	}

	@Bean
    public GeminiApiService geminiApiService(GeminiHistoryRepository geminiHistoryRepository) {
        return new GeminiApiService(geminiHistoryRepository);
    }
}
