package com.example.user_auth.dto;

public class PubMedArticle {
    private String title;
    private String shortDescription;
    private String authors;
    private String publicationDate;
    private String pubMedId;
    private String pubMedLink;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getPubMedId() {
        return pubMedId;
    }

    public void setPubMedId(String pubMedId) {
        this.pubMedId = pubMedId;
    }

    public String getPubMedLink() {
        return pubMedLink;
    }

    public void setPubMedLink(String pubMedLink) {
        this.pubMedLink = pubMedLink;
    }
}