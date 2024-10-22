package com.hhnatsiuk.mypastebin_backend.service;

import com.hhnatsiuk.mypastebin_backend.dto.LoginDTO;
import com.hhnatsiuk.mypastebin_backend.dto.LoginResponse;
import com.hhnatsiuk.mypastebin_backend.dto.ProfileDTO;
import com.hhnatsiuk.mypastebin_backend.dto.SignUpDTO;
import com.hhnatsiuk.mypastebin_backend.entity.Post;
import com.hhnatsiuk.mypastebin_backend.entity.User;
import com.hhnatsiuk.mypastebin_backend.repository.PostRepository;
import com.hhnatsiuk.mypastebin_backend.repository.UserRepository;
import com.hhnatsiuk.mypastebin_backend.utils.JwtTokenUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthService(UserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public LoginResponse login(LoginDTO loginDTO) {
        Optional<User> userOptional = userRepository.findByUsername(loginDTO.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                user.setLastLoginAt(OffsetDateTime.now());
                userRepository.save(user);

                String token = jwtTokenUtil.generateToken(user);

                List<Post> userPosts = postRepository.findByUser(user);

                ProfileDTO profileDTO = new ProfileDTO(user, userPosts);

                LoginResponse response = new LoginResponse();
                response.setToken(token);
                response.setExpiresIn(jwtTokenUtil.getExpirationTime());
                response.setUser(profileDTO);

                return response;
            }
        }

        throw new RuntimeException("Invalid username or password");
    }

    public User signup(SignUpDTO signUpDTO) {
        logger.debug("Attempting to sign up a new user with email: {} and username: {}", signUpDTO.getEmail(), signUpDTO.getUsername());

        if (userRepository.existsByEmail(signUpDTO.getEmail()) || userRepository.existsByUsername(signUpDTO.getUsername())) {
            logger.warn("User with email: {} or username: {} already exists", signUpDTO.getEmail(), signUpDTO.getUsername());
            throw new RuntimeException("User already exists with the given email or username");
        }

        logger.debug("Creating new user with username: {}", signUpDTO.getUsername());

        User user = User.builder()
                .username(signUpDTO.getUsername())
                .email(signUpDTO.getEmail())
                .password(passwordEncoder.encode(signUpDTO.getPassword()))
                .createdAt(OffsetDateTime.now())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        logger.debug("User with username: {} and email: {} successfully saved", savedUser.getUsername(), savedUser.getEmail());

        return savedUser;
    }

}
