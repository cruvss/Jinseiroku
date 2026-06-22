package com.cruvs.backend.service;

import com.cruvs.backend.dto.atuh.AuthResponse;
import com.cruvs.backend.dto.atuh.LoginRequest;
import com.cruvs.backend.dto.atuh.RegisterRequest;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.entity.UserSession;
import com.cruvs.backend.repository.UserRepository;
import com.cruvs.backend.repository.UserSessionRepository;
import com.cruvs.backend.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom secureRandom = new SecureRandom();

    public String refreshToken = jwtTokenProvider.generateRefreshToken(); //make this private later
    public String refreshTokenHash = passwordEncoder.encode(refreshToken);


    private String generateRandomString(int length){
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request){

        if (userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already registered");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        String encryptionSalt = generateRandomString(32);
        String recoveryKey = generateRandomString(32);
        String recoveryKeyHash = passwordEncoder.encode(recoveryKey);

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .encryptionSalt(encryptionSalt)
                .recoveryKeyHash(recoveryKeyHash)
                .timezone("UTC")
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}",user.getEmail());

        return AuthResponse.builder()
                .recoveryKey(recoveryKey)
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(),user.getPasswordHash())){
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
//        String refreshToken = jwtTokenProvider.generateRefreshToken();
//        String refreshTokenHash = passwordEncoder.encode(refreshToken);

        UserSession session = UserSession.builder()
                .user(user)
                .refreshTokenHash(refreshTokenHash)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        userSessionRepository.save(session);
        log.info("User logged in {}: ",user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token required");
        }

        String refreshTokenHash = passwordEncoder.encode(refreshToken);
        System.out.println("converted Hash");
        System.out.println(refreshTokenHash);
        UserSession session = userSessionRepository.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSessionRepository.delete(session);
            throw new IllegalArgumentException("Refresh token expired");
        }


        String newAccessToken = jwtTokenProvider.generateAccessToken(session.getUser().getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .email(session.getUser().getEmail())
                .build();
    }

    @Transactional
    public void logout(String refreshToken){
        if (refreshToken !=null){
            String refreshTokenHash = passwordEncoder.encode(refreshToken);
            userSessionRepository.findByRefreshTokenHash(refreshTokenHash)
                    .ifPresent(userSessionRepository::delete);
        }
    }

    public String generateRefreshToken() {
        return jwtTokenProvider.generateRefreshToken();
    }




}
