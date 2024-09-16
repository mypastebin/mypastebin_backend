package com.hhnatsiuk.mypastebin_backend.service;

import com.hhnatsiuk.mypastebin_backend.dto.PostDTO;
import com.hhnatsiuk.mypastebin_backend.entity.Post;
import com.hhnatsiuk.mypastebin_backend.repository.PostRepository;
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

@Service
public class PostService {

    private static final Logger logger = LogManager.getLogger(PostService.class);
    private static final String FILE_EXTENSION = ".txt";

    private final PostRepository postRepository;
    private final HashServiceClient hashServiceClient;
    private final GoogleCloudStorageService googleCloudStorageService;

    @Autowired
    public PostService(PostRepository postRepository,
                       HashServiceClient hashServiceClient,
                       GoogleCloudStorageService googleCloudStorageService) {
        this.postRepository = postRepository;
        this.hashServiceClient = hashServiceClient;
        this.googleCloudStorageService = googleCloudStorageService;
    }

    public Post createPost(Post post) {
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

    public Optional<PostDTO> getPostDTOByHash(String hash) {
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

            return Optional.of(postDTO);
        } else {
            return Optional.empty();
        }
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
