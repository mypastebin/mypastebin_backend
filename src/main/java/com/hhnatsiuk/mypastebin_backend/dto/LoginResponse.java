package com.hhnatsiuk.mypastebin_backend.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private int expiresIn;
    private ProfileDTO user;
}

