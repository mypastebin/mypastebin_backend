package com.hhnatsiuk.mypastebin_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column
    private String password;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    private Integer rating = 0;

    private Integer views = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    private Boolean oauth2User = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
