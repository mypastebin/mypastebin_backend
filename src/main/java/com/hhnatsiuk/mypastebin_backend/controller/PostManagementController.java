package com.hhnatsiuk.mypastebin_backend.controller;

import com.hhnatsiuk.mypastebin_backend.dto.PostDTO;
import com.hhnatsiuk.mypastebin_backend.exception.NotFoundException;
import com.hhnatsiuk.mypastebin_backend.exception.UnauthorizedException;
import com.hhnatsiuk.mypastebin_backend.service.PostService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173")
public class PostManagementController {

    private static final Logger logger = LogManager.getLogger(PostManagementController.class);

    private final PostService postService;

    @Autowired
    public PostManagementController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO, @RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        try {
            PostDTO createdPostDTO = postService.createPost(postDTO, tokenHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPostDTO);
        } catch (UnauthorizedException e) {
            logger.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("Error occurred while creating post: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{hash}")
    public ResponseEntity<PostDTO> getPost(@PathVariable String hash) {
        try {
            PostDTO postDTO = postService.getPostByHash(hash);
            return ResponseEntity.ok(postDTO);
        } catch (NotFoundException e) {
            logger.warn("Post not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error occurred while retrieving post: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{hash}")
    public ResponseEntity<Void> deletePost(@PathVariable String hash) {
        try {
            postService.deletePost(hash);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error occurred while deleting post: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PostDTO>> getRecentPosts() {
        try {
            List<PostDTO> postDTOs = postService.getRecentPostsDTO();
            return ResponseEntity.ok(postDTOs);
        } catch (Exception e) {
            logger.error("An error occurred while retrieving recent posts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{hash}/increment-views")
    public ResponseEntity<Void> incrementPostViews(@PathVariable String hash) {
        try {
            postService.incrementPostViews(hash);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error incrementing views for post with hash: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
