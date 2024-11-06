package com.hhnatsiuk.mypastebin_backend.controller;

import com.hhnatsiuk.mypastebin_backend.dto.PostDTO;
import com.hhnatsiuk.mypastebin_backend.entity.Post;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.repository.UserRepository;
import com.hhnatsiuk.mypastebin_backend.service.PostService;
import com.hhnatsiuk.mypastebin_backend.utils.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173")
public class PostManagementController {

    private static final Logger logger = LogManager.getLogger(PostManagementController.class);

    private final PostService postService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;


    @Autowired
    public PostManagementController(PostService postService, JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.postService = postService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody Post post, @RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        logger.info("Received POST request to create a new post");
        logger.debug("Request details: title = {}, category = {}, is empty content = {}, expirationDate = {}",
                post.getTitle(), post.getCategory(), post.getContent().isEmpty(), post.getExpirationDate());

        try {
            if (tokenHeader != null && !tokenHeader.isEmpty()) {
                String token = jwtTokenUtil.extractTokenFromHeader(tokenHeader);
                String username = jwtTokenUtil.extractUsername(token);

                Optional<User> userOptional = userRepository.findByUsername(username);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    post.setUser(user);
                    logger.info("Post will be associated with user: " + user.getUsername());
                } else {
                    logger.error("User not found for username: {}", username);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            } else {
                logger.info("No Authorization header provided, proceeding without user association.");
            }

            Post createdPost = postService.createPost(post);
            logger.info("Post created successfully with ID: {}", createdPost.getId());

            PostDTO postDTO = new PostDTO();
            postDTO.setHash(createdPost.getHash());
            postDTO.setTitle(createdPost.getTitle());
            postDTO.setCategory(createdPost.getCategory());
            postDTO.setExpirationDate(createdPost.getExpirationDate().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(postDTO);
        } catch (Exception e) {
            logger.error("Error occurred while creating post: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/{hash}")
    public ResponseEntity<PostDTO> getPost(@PathVariable String hash) {
        logger.info("Received GET request to retrieve post with hash: {}", hash);

        try {
            Optional<PostDTO> postDTO = postService.getPostDTOByHash(hash);

            if (postDTO.isPresent()) {
                logger.info("Post retrieved successfully for hash: {}", hash);
                return ResponseEntity.ok(postDTO.get());
            } else {
                logger.warn("No post found for hash: {}", hash);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("Error occurred while retrieving post: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{hash}")
    public ResponseEntity<Void> deletePost(@PathVariable String hash) {
        logger.info("Received DELETE request for post with hash: {}", hash);

        try {
            postService.deletePost(hash);
            logger.info("Post with hash: {} deleted successfully", hash);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error occurred while deleting post with hash: {}", hash, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PostDTO>> getRecentPosts() {
        logger.info("Received request to get recent posts");

        try {
            List<Post> posts = postService.getRecentPosts();
            logger.info("Successfully retrieved {} recent posts", posts.size());

            List<PostDTO> postDTOs = posts.stream()
                    .map(post -> {
                        PostDTO dto = new PostDTO();
                        dto.setHash(post.getHash());
                        dto.setTitle(post.getTitle());
                        dto.setCategory(post.getCategory());
                        dto.setCreatedAt(post.getCreatedAt().toString());
                        dto.setExpirationDate(post.getExpirationDate().toString());
                        dto.setViews(post.getViews());
                        dto.setFileSize(post.getFileSize());
                        return dto;
                    })
                    .toList();

            postDTOs.forEach(postDTO -> logger.debug("PostDTO details: {}", postDTO));
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
            logger.error("Error incrementing views for post with hash: {}", hash, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
