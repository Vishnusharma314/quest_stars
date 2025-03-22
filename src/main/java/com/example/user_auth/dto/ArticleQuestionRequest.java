package com.example.user_auth.dto;

public class ArticleQuestionRequest {
    private String pubmedArticle;
    private String userQuestion;

    public String getPubmedArticle() {
        return pubmedArticle;
    }

    public void setPubmedArticle(String pubmedArticle) {
        this.pubmedArticle = pubmedArticle;
    }

    public String getUserQuestion() {
        return userQuestion;
    }

    public void setUserQuestion(String userQuestion) {
        this.userQuestion = userQuestion;
    }
}