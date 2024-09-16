package com.hhnatsiuk.mypastebin_backend.dto;

import lombok.Data;

@Data
public class PostDTO {
    private String title;
    private String category;
    private String hash;
    private String createdAt;
    private String expirationDate;
    private int views;
    private String content;
    private Long fileSize;
}
