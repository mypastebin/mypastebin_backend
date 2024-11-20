package com.hhnatsiuk.mypastebin_backend.service;

import com.hhnatsiuk.mypastebin_backend.dto.PostDTO;
import com.hhnatsiuk.mypastebin_backend.entity.Post;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.exception.NotFoundException;
import com.hhnatsiuk.mypastebin_backend.exception.UnauthorizedException;
import com.hhnatsiuk.mypastebin_backend.repository.PostRepository;
import com.hhnatsiuk.mypastebin_backend.repository.UserRepository;
import com.hhnatsiuk.mypastebin_backend.utils.JwtTokenUtil;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger logger = LogManager.getLogger(PostService.class);
    private static final String FILE_EXTENSION = ".txt";

    private final PostRepository postRepository;
    private final HashGeneratorService hashServiceClient;
    private final GoogleCloudStorageService googleCloudStorageService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository,
                       HashGeneratorService hashServiceClient,
                       GoogleCloudStorageService googleCloudStorageService,
                       JwtTokenUtil jwtTokenUtil,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.hashServiceClient = hashServiceClient;
        this.googleCloudStorageService = googleCloudStorageService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    public PostDTO createPost(PostDTO postDTO, String tokenHeader) throws UnauthorizedException, Exception {
        logger.info("Received request to create a new post");
        logger.debug("Request details: title = {}, category = {}, is empty content = {}, expirationDate = {}",
                postDTO.getTitle(), postDTO.getCategory(), postDTO.getContent().isEmpty(), postDTO.getExpirationDate());

        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setCategory(postDTO.getCategory());
        post.setContent(postDTO.getContent());
        post.setExpirationDate(OffsetDateTime.parse(postDTO.getExpirationDate()));

        if (tokenHeader != null && !tokenHeader.isEmpty()) {
            String token = jwtTokenUtil.extractTokenFromHeader(tokenHeader);
            String username = jwtTokenUtil.extractUsername(token);

            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                post.setUser(user);
                logger.info("Post will be associated with user: {}", user.getUsername());
            } else {
                logger.error("User not found for username: {}", username);
                throw new UnauthorizedException("User not found for username: " + username);
            }
        } else {
            logger.info("No Authorization header provided, proceeding without user association.");
        }

        Post createdPost = savePost(post);

        logger.info("Post created successfully with ID: {}", createdPost.getId());

        PostDTO createdPostDTO = new PostDTO();
        createdPostDTO.setHash(createdPost.getHash());
        createdPostDTO.setTitle(createdPost.getTitle());
        createdPostDTO.setCategory(createdPost.getCategory());
        createdPostDTO.setExpirationDate(createdPost.getExpirationDate().toString());

        return createdPostDTO;
    }

    public PostDTO getPostByHash(String hash) throws NotFoundException, Exception {
        logger.info("Received request to retrieve post with hash: {}", hash);

        Optional<Post> postOptional = postRepository.findByHash(hash);

        if (postOptional.isPresent()) {
            Post post = postOptional.get();

            String content = googleCloudStorageService.downloadFile(post.getHash() + FILE_EXTENSION);

            PostDTO postDTO = new PostDTO();
            postDTO.setTitle(post.getTitle());
            postDTO.setCategory(post.getCategory());
            postDTO.setHash(post.getHash());
            postDTO.setCreatedAt(post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            postDTO.setExpirationDate(post.getExpirationDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            postDTO.setViews(post.getViews());
            postDTO.setContent(content);
            postDTO.setFileSize(post.getFileSize());

            logger.info("Post retrieved successfully for hash: {}", hash);
            return postDTO;
        } else {
            logger.warn("No post found for hash: {}", hash);
            throw new NotFoundException("No post found for hash: " + hash);
        }
    }

    public List<PostDTO> getRecentPostsDTO() throws Exception {
        logger.info("Received request to get recent posts");

        List<Post> posts = getRecentPosts();

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
                .collect(Collectors.toList());

        postDTOs.forEach(postDTO -> logger.debug("PostDTO details: {}", postDTO));

        return postDTOs;
    }

    public Post savePost(Post post) {
        String hash = hashServiceClient.generateUniqueHash();
        post.setHash(hash);

        String fileName = hash + FILE_EXTENSION;
        byte[] contentBytes = post.getContent().getBytes(StandardCharsets.UTF_8);
        String textUrl = googleCloudStorageService.uploadFile(contentBytes, "text/plain", fileName, post);

        logger.debug("File '{}' was uploaded", fileName);

        post.setContent(null);
        post.setTextUrl(textUrl);

        return postRepository.save(post);
    }

    public List<Post> getRecentPosts() {
        List<Post> posts = postRepository.findTop10ByOrderByCreatedAtDesc();
        return posts != null ? posts : new ArrayList<>();
    }

    public void deletePost(String hash) {
        Optional<Post> post = postRepository.findByHash(hash);
        if (post.isPresent()) {
            googleCloudStorageService.deleteFile(hash + FILE_EXTENSION);
            postRepository.deleteByHash(hash);
            logger.debug("Post with hash '{}' was deleted", hash);
        } else {
            logger.debug("Post with hash '{}' not found", hash);
        }
    }

    public List<Post> getPostsToDelete(OffsetDateTime currentDateTime) {
        return postRepository.findByExpirationDateBefore(currentDateTime);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanUpExpiredPosts() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Post> expiredPosts = getPostsToDelete(now);

        for (Post post : expiredPosts) {
            deletePost(post.getHash());
            logger.debug("Expired post with hash '{}' was deleted", post.getHash());
        }
    }

    public void incrementPostViews(String hash) throws Exception {
        Optional<Post> postOptional = postRepository.findByHash(hash);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            post.setViews(post.getViews() + 1);
            postRepository.save(post);
        } else {
            throw new Exception("Post not found with hash: " + hash);
        }
    }
}
