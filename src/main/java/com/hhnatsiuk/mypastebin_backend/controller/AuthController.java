package com.hhnatsiuk.mypastebin_backend.controller;

import com.hhnatsiuk.mypastebin_backend.dto.LoginDTO;
import com.hhnatsiuk.mypastebin_backend.response.LoginResponse;
import com.hhnatsiuk.mypastebin_backend.dto.SignUpDTO;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @Operation(
            summary = "Log in a user",
            description = "Authenticates a user with their username and password and returns a login response.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful login",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
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

    @Operation(
            summary = "Sign up a new user",
            description = "Registers a new user with the provided email, username, and password.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "User already exists with the given email or username",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
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
