package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.request.LoginRequest;
import com.jinian.blog.dto.request.RegisterRequest;
import com.jinian.blog.dto.response.JwtResponse;
import com.jinian.blog.dto.response.UserResponse;
import com.jinian.blog.service.AuthService;
import com.jinian.blog.util.RsaUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证", description = "登录注册相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RsaUtils rsaUtils;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<JwtResponse> refreshToken(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }

    @Operation(summary = "获取RSA公钥")
    @GetMapping("/public-key")
    public Result<String> getPublicKey() {
        return Result.success(rsaUtils.getPublicKeyBase64());
    }
}