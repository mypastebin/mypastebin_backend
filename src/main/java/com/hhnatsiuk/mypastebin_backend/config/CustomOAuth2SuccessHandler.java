package com.hhnatsiuk.mypastebin_backend.config;

import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.repository.UserRepository;
import com.hhnatsiuk.mypastebin_backend.utils.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LogManager.getLogger(CustomOAuth2SuccessHandler.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final String redirectUrl = "http://localhost:5173/oauth2/redirect?token=";

    public CustomOAuth2SuccessHandler(JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String email = oauthToken.getPrincipal().getAttribute("email");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email.split("@")[0]);
            newUser.setIsActive(true);
            newUser.setOauth2User(true);
            return userRepository.save(newUser);
        });

        String token = jwtTokenUtil.generateToken(user);


        logger.info("Redirecting to frontend: {}", redirectUrl + token);

        response.sendRedirect(redirectUrl + token);
    }
}