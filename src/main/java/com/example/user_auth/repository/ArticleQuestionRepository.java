package com.example.user_auth.repository;

import com.example.user_auth.model.ArticleQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticleQuestionRepository extends JpaRepository<ArticleQuestion, Long> {
    List<ArticleQuestion> findByUserId(Long userId);
}