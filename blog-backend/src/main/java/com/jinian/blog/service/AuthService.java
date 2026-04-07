package com.jinian.blog.service;

import com.jinian.blog.dto.request.LoginRequest;
import com.jinian.blog.dto.request.RegisterRequest;
import com.jinian.blog.dto.response.JwtResponse;
import com.jinian.blog.dto.response.UserResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    JwtResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    JwtResponse register(RegisterRequest request);

    /**
     * 刷新Token
     */
    JwtResponse refreshToken(String refreshToken);

    /**
     * 获取当前用户信息
     */
    UserResponse getCurrentUser();

    /**
     * 更新最后登录时间
     */
    void updateLastLogin(Long userId);
}