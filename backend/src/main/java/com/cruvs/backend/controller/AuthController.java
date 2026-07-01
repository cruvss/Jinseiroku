package com.cruvs.backend.controller;

import com.cruvs.backend.dto.atuh.*;
import com.cruvs.backend.repository.UserSessionRepository;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.AuthService;
import com.cruvs.backend.util.ApiResponseUtil;
import com.cruvs.backend.util.GetAuthUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserSessionRepository userSessionRepository;
    private final GetAuthUser authUser;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtil.created("User registered successfully",response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request,response);


        Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());

        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
//        refreshCookie.setPath("/v1/auth/logout");
        refreshCookie.setMaxAge(7*24*60*60);
        response.addCookie(refreshCookie);

        authResponse.setRecoveryKey(null);
        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(ApiResponseUtil.success("Login Successful",authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        AuthResponse response = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponseUtil.success("Token refreshed", response));

    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        log.info(Arrays.toString(cookies));
        if (cookies!=null){
            for (Cookie cookie: cookies){
                if ("refreshToken".equals(cookie.getName())){
                    log.info(cookie.getName());
                    authService.logout(cookie.getValue());
                    Cookie refreshTokenCookie = new Cookie("refreshToken", null);
                    refreshTokenCookie.setPath("/");
                    refreshTokenCookie.setMaxAge(0);
                    response.addCookie(refreshTokenCookie);
                    break;
                }
            }
        }
        return ResponseEntity.ok(ApiResponseUtil.success("Logout Successful",null));
    }

    @GetMapping("/salt")
    public ResponseEntity<ApiResponse<VaultParamsResponse>> getSalt(@RequestParam("email") String email) {
        VaultParamsResponse params = authService.getVaultParamsByEmail(email);
        return ResponseEntity.ok(ApiResponseUtil.success("Salt retrieved successfully", params));
    }

    @PutMapping("/kek-verification")
    public ResponseEntity<ApiResponse<Void>> updateKekVerification(@RequestBody String verification) {
        UUID userId = (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        authService.updateKekVerification(userId, verification);
        return ResponseEntity.ok(ApiResponseUtil.success("KEK verification token updated successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(){
        UserDto userDto = authService.getUserProfile(authUser.getAuthenticatedUserId());

        return ResponseEntity.ok(ApiResponseUtil.success("User profile retrieved", userDto));
    }

}