package com.hhnatsiuk.mypastebin_backend.response;

import com.hhnatsiuk.mypastebin_backend.dto.ProfileDTO;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private int expiresIn;
    private ProfileDTO user;
}

