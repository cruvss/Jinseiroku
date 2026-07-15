package com.cruvs.backend.service;

import com.cruvs.backend.dto.atuh.*;
import com.cruvs.backend.entity.SubscriptionPlan;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.entity.UserSession;
import com.cruvs.backend.exception.BusinessRuleException;
import com.cruvs.backend.exception.InvalidTokenException;
import com.cruvs.backend.exception.ResourceNotFoundException;
import com.cruvs.backend.repository.SubscriptionPlanRepository;
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

import java.lang.module.ResolutionException;
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
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private static final SecureRandom secureRandom = new SecureRandom();

    private String generateRandomString(int length){
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request){

        log.debug("Register attempt for email: {}",request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())){
            log.warn("Registration failed - email already exists: {}",request.getEmail());
            throw new BusinessRuleException("Email already registered");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        String encryptionSalt = generateRandomString(32);
        String recoveryKey = generateRandomString(32);
        String recoveryKeyHash = passwordEncoder.encode(recoveryKey);

        SubscriptionPlan freePlan = subscriptionPlanRepository.findById(UUID.fromString("b199d750-a9cf-4bc1-9f93-4a6c8e310001"))
                .orElseThrow(()-> new ResolutionException("Default subscription plan not configured"));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .encryptionSalt(encryptionSalt)
                .recoveryKeyHash(recoveryKeyHash)
                .subscriptionPlan(freePlan)
                .timezone("UTC")
                .build();

        user = userRepository.save(user);
        log.info("User registered with email: {}",user.getEmail());

        return AuthResponse.builder()
                .recoveryKey(recoveryKey)
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response){

        log.debug("Login attempt for email: {}",request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(),user.getPasswordHash())){
            log.warn("Failed login attempt - wrong password for: {}",request.getEmail());
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
            log.warn("Refresh token null");
            throw new InvalidTokenException("Refresh token required");
        }
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Refresh token invalid");
            throw new InvalidTokenException("Invalid refresh token");
        }
        UUID sessionId = jwtTokenProvider.getSessionIdFromToken(refreshToken);
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new InvalidTokenException("Invalid session or session revoked"));
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSessionRepository.delete(session);
            log.warn("Refresh token expired");
            throw new InvalidTokenException("Refresh token expired");
        }
        String newAccessToken = jwtTokenProvider.generateAccessToken(session.getUser().getId());
        log.debug("Token refreshed for userId: {}",session.getUser().getEmail());
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .email(session.getUser().getEmail())
                .build();
    }

    public String getSaltByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getEncryptionSalt)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public VaultParamsResponse getVaultParamsByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> VaultParamsResponse.builder()
                        .encryptionSalt(user.getEncryptionSalt())
                        .encryptedKekVerification(user.getEncryptedKekVerification())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public void updateKekVerification(java.util.UUID userId, String verification) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User",userId));
        user.setEncryptedKekVerification(verification);
        log.info("KEK verification updated for userId: {}",user.getEmail());
        userRepository.save(user);
    }



    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            UUID sessionId = jwtTokenProvider.getSessionIdFromToken(refreshToken);
            userSessionRepository.deleteById(sessionId);
            log.info("User logged out successfully. Session revoked: {}", sessionId);
        } else{
            log.warn("Logout request received with an invalid or missing refresh token.");

        }
    }

    public UserDto getUserProfile(UUID userId){
        log.debug("Fetching profile for userId: {}",userId);
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User",userId));

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .timezone(user.getTimezone())
                .subscriptionPlanId(user.getSubscriptionPlan() !=null ?
                        user.getSubscriptionPlan().getId() : null)
                .subscriptionName(subscriptionPlanService.getPlanName(user.getSubscriptionPlan().getId()))
                .createdAt(user.getCreatedAt())
                .build();
    }

}
