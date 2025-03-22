package com.example.user_auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.user_auth.model.GeminiHistory;

public interface GeminiHistoryRepository extends JpaRepository<GeminiHistory, Long> {
     List<GeminiHistory> findByUserIdOrderByIdDesc(Long userId);
}