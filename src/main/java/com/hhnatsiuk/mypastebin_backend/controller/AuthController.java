package com.hhnatsiuk.mypastebin_backend.controller;

import com.hhnatsiuk.mypastebin_backend.dto.LoginDTO;
import com.hhnatsiuk.mypastebin_backend.response.LoginResponse;
import com.hhnatsiuk.mypastebin_backend.dto.SignUpDTO;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.service.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
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
        try {
            LoginResponse response = authService.login(loginDTO);
            return ResponseEntity.ok().body(response);
        } catch (RuntimeException e) {
            logger.warn("Failed login attempt for username: {}", loginDTO.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpDTO signUpDTO) {
        logger.debug("Received signup request for email: {} and username: {}", signUpDTO.getEmail(), signUpDTO.getUsername());

        try {
            User user = authService.signup(signUpDTO);
            logger.info("User successfully signed up with username: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            logger.warn("Signup failed for email: {} or username: {}", signUpDTO.getEmail(), signUpDTO.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists with the given email or username");
        }
    }

}
