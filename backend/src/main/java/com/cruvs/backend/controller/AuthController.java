package com.cruvs.backend.controller;

import com.cruvs.backend.dto.atuh.AuthResponse;
import com.cruvs.backend.dto.atuh.LoginRequest;
import com.cruvs.backend.dto.atuh.RegisterRequest;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.AuthService;
import com.cruvs.backend.util.ApiResponseUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtil.created("User registered sucessfully",response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request,response);


        Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());

        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/v1/auth/refresh");
        refreshCookie.setMaxAge(7*24*60*60);
        response.addCookie(refreshCookie);

        authResponse.setRecoveryKey(null);
        authResponse.setRefreshToken(null);
        return ResponseEntity.ok(ApiResponseUtil.success("Login Sucessful",authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    System.out.println("refreshtoken from cookie");
                    System.out.println(refreshToken);
                    break;
                }
            }
        }

        AuthResponse response = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponseUtil.success("Token refreshed", response));

    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies!=null){
            for (Cookie cookie: cookies){
                if ("refreshToken".equals(cookie.getName())){
                    authService.logout(cookie.getValue());
                    break;
                }
            }
        }
        return ResponseEntity.ok(ApiResponseUtil.success("Logout Successful",null));

    }

    @GetMapping("/salt")
    public ResponseEntity<ApiResponse<String>> getSalt(@RequestParam("email") String email) {
        String salt = authService.getSaltByEmail(email);
        return ResponseEntity.ok(ApiResponseUtil.success("Salt retrieved successfully", salt));
    }

}