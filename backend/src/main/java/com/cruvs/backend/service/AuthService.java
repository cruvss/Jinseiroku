package com.cruvs.backend.service;

import com.cruvs.backend.dto.atuh.AuthResponse;
import com.cruvs.backend.dto.atuh.LoginRequest;
import com.cruvs.backend.dto.atuh.RegisterRequest;
import com.cruvs.backend.dto.atuh.VaultParamsResponse;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom secureRandom = new SecureRandom();

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

        UserSession session = UserSession.builder()
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        session = userSessionRepository.save(session);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), session.getId());

        log.info("User logged in: {}",user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .encryptionSalt(user.getEncryptionSalt())
                .encryptedKekVerification(user.getEncryptedKekVerification())
                .build();

    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token required");
        }
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        UUID sessionId = jwtTokenProvider.getSessionIdFromToken(refreshToken);
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session or session revoked"));
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

    public String getSaltByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getEncryptionSalt)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public VaultParamsResponse getVaultParamsByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> VaultParamsResponse.builder()
                        .encryptionSalt(user.getEncryptionSalt())
                        .encryptedKekVerification(user.getEncryptedKekVerification())
                        .build())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public void updateKekVerification(java.util.UUID userId, String verification) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEncryptedKekVerification(verification);
        userRepository.save(user);
    }



    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            UUID sessionId = jwtTokenProvider.getSessionIdFromToken(refreshToken);
            userSessionRepository.deleteById(sessionId);
        }
    }

}
