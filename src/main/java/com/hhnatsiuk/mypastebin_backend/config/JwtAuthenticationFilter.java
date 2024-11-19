package com.hhnatsiuk.mypastebin_backend.config;

import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.repository.UserRepository;
import com.hhnatsiuk.mypastebin_backend.utils.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        String requestURI = request.getRequestURI();
        logger.info("Incoming request to URI: " + requestURI);
        logger.info("Authorization Header: " + (authorizationHeader != null ? authorizationHeader : "No Authorization Header"));

        // FIXME
        if (requestURI.equals("/api/posts") || requestURI.equals("/api/posts/recent") || requestURI.matches("/api/posts/.*")
                ||  requestURI.matches("/api/auth/google/callback") || requestURI.startsWith("/oauth2/") || requestURI.equals("/api/auth/google/success")
                ||  requestURI.startsWith("/oauth2/") || requestURI.startsWith("/login/oauth2/") || requestURI.equals("/api/auth/google/success")) {
            logger.info("Public endpoint access allowed without token for URI: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                username = jwtTokenUtil.extractUsername(token);
                logger.info("Extracted username from token: " + username);
            } catch (Exception e) {
                logger.error("JWT Token extraction failed. Exception: ", e);
            }
        } else {
            logger.warn("Authorization header is missing or does not start with 'Bearer '. Access denied.");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isPresent() && jwtTokenUtil.isTokenValid(token, userOptional.get())) {
                User user = userOptional.get();
                logger.info("User found in repository: " + user.getUsername());

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        user, null, new ArrayList<>());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.info("Authentication set for user: " + user.getUsername());
            } else {
                logger.warn("Token is invalid or user not found in the repository.");
            }
        } else {
            if (username == null) {
                logger.warn("Username is null after token extraction.");
            }
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                logger.info("Security context already contains authentication for user.");
            }
        }

        logger.info("Proceeding with filter chain for URI: " + requestURI);
        filterChain.doFilter(request, response);
    }
}

