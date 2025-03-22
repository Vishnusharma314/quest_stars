package com.example.user_auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.user_auth.model.GeminiHistory;
import java.util.List;

public interface GeminiHistoryRepository extends JpaRepository<GeminiHistory, Long> {
     List<GeminiHistory> findByUserId(Long userId);
}