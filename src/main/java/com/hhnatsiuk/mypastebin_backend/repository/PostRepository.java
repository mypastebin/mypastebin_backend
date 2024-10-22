package com.hhnatsiuk.mypastebin_backend.repository;

import com.hhnatsiuk.mypastebin_backend.entity.Post;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByHash(String hash);
    List<Post> findTop10ByOrderByCreatedAtDesc();
    List<Post> findByExpirationDateBefore(OffsetDateTime currentDateTime);
    void deleteByHash(String hash);
    List<Post> findByUser(User user);
}
