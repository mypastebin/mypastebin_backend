package com.hhnatsiuk.mypastebin_backend.controller;

import com.hhnatsiuk.mypastebin_backend.dto.ProfileDTO;
import com.hhnatsiuk.mypastebin_backend.entity.Post;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.service.ProfileService;
import com.hhnatsiuk.mypastebin_backend.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;
    private final JwtTokenUtil jwtTokenUtil;

    public ProfileController(ProfileService profileService, JwtTokenUtil jwtTokenUtil) {
        this.profileService = profileService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

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
