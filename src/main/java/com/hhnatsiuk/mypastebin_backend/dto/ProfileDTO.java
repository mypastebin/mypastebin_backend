package com.hhnatsiuk.mypastebin_backend.dto;

import com.hhnatsiuk.mypastebin_backend.entity.Post;
import com.hhnatsiuk.mypastebin_backend.entity.User;

import java.util.List;

import lombok.Data;

@Data
public class ProfileDTO {
    private String username;
    private String email;
    private Integer views;
    private Integer rating;
    private List<Post> posts;

    public ProfileDTO(User user, List<Post> posts) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.views = user.getViews();
        this.rating = user.getRating();
        this.posts = posts;
    }

}
