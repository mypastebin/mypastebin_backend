package com.hhnatsiuk.mypastebin_backend.controller;

import com.hhnatsiuk.mypastebin_backend.dto.LoginDTO;
import com.hhnatsiuk.mypastebin_backend.dto.LoginResponse;
import com.hhnatsiuk.mypastebin_backend.dto.SignUpDTO;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.service.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        LoginResponse response = authService.login(loginDTO);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpDTO signUpDTO) {
        logger.debug("Received signup request for email: {} and username: {}", signUpDTO.getEmail(), signUpDTO.getUsername());

        User user = authService.signup(signUpDTO);

        logger.info("User successfully signed up with username: {}", user.getUsername());

        return ResponseEntity.ok().body(user);
    }

}
