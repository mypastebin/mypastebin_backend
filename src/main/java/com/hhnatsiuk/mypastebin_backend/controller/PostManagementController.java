package com.hhnatsiuk.mypastebin_backend.controller;

import com.hhnatsiuk.mypastebin_backend.dto.PostDTO;
import com.hhnatsiuk.mypastebin_backend.exception.NotFoundException;
import com.hhnatsiuk.mypastebin_backend.exception.UnauthorizedException;
import com.hhnatsiuk.mypastebin_backend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Post Management", description = "Endpoints for managing posts")
public class PostManagementController {

    private static final Logger logger = LogManager.getLogger(PostManagementController.class);

    private final PostService postService;

    @Autowired
    public PostManagementController(PostService postService) {
        this.postService = postService;
    }


    @Operation(
            summary = "Create a new post",
            description = "Creates a new post using the provided PostDTO. Requires an optional Authorization token.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Post created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized access"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
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


    @Operation(
            summary = "Retrieve a post by hash",
            description = "Fetches a post using its unique hash.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
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


    @Operation(
            summary = "Delete a post by hash",
            description = "Deletes a post using its unique hash.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Post deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
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


    @Operation(
            summary = "Retrieve recent posts",
            description = "Fetches a list of the most recent posts.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Recent posts retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
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


    @Operation(
            summary = "Increment post views",
            description = "Increments the view count for a post identified by its hash.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Views incremented successfully"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
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
