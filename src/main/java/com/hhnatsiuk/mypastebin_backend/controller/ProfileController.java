package com.hhnatsiuk.mypastebin_backend.controller;

import com.hhnatsiuk.mypastebin_backend.dto.ProfileDTO;
import com.hhnatsiuk.mypastebin_backend.entity.Post;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.service.ProfileService;
import com.hhnatsiuk.mypastebin_backend.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Profile Management", description = "Endpoints for managing user profiles")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;
    private final JwtTokenUtil jwtTokenUtil;

    public ProfileController(ProfileService profileService, JwtTokenUtil jwtTokenUtil) {
        this.profileService = profileService;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    @Operation(
            summary = "Get the profile of the currently logged-in user",
            description = "Fetches the profile details and posts of the user identified by the JWT token in the Authorization header.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Profile retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @GetMapping("/api/profile")
    public ResponseEntity<ProfileDTO> getProfile(@RequestHeader("Authorization") String tokenHeader) {
        String token = jwtTokenUtil.extractTokenFromHeader(tokenHeader);
        String username = jwtTokenUtil.extractUsername(token);

        User user = profileService.getUserByUsername(username);
        if (user == null) {
            logger.warn("User not found with username: {}", username);
            return ResponseEntity.notFound().build();
        }

        logger.info("User found: {}", user.getUsername());

        List<Post> posts = profileService.getUserPosts(user);

        ProfileDTO profile = new ProfileDTO(user, posts);
        logger.info("ProfileDTO created for user: {}", user.getUsername());

        return ResponseEntity.ok(profile);
    }


    @Operation(
            summary = "Get the profile of a user by username",
            description = "Fetches the profile details and posts of a user by their username.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Profile retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @GetMapping("/api/profile/{username}")
    public ResponseEntity<ProfileDTO> getProfileByUsername(@PathVariable("username") String username) {
        User user = profileService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Post> posts = profileService.getUserPosts(user);
        ProfileDTO profile = new ProfileDTO(user, posts);

        return ResponseEntity.ok(profile);
    }

}
