package com.jinian.blog.service.impl;

import com.jinian.blog.common.exception.BusinessException;
import com.jinian.blog.dto.request.LoginRequest;
import com.jinian.blog.dto.request.RegisterRequest;
import com.jinian.blog.dto.response.JwtResponse;
import com.jinian.blog.dto.response.UserResponse;
import com.jinian.blog.entity.Role;
import com.jinian.blog.entity.User;
import com.jinian.blog.mapper.RoleMapper;
import com.jinian.blog.mapper.UserMapper;
import com.jinian.blog.security.UserDetailsImpl;
import com.jinian.blog.service.AuthService;
import com.jinian.blog.util.JwtUtils;
import com.jinian.blog.util.RsaUtils;
import com.jinian.blog.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RsaUtils rsaUtils;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public JwtResponse login(LoginRequest request) {
        // RSA解密密码
        String rawPassword = rsaUtils.decrypt(request.getPassword());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), rawPassword)
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        String accessToken = jwtUtils.generateToken(userDetails.getId(), userDetails.getUsername(), roles);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getId(), userDetails.getUsername());

        updateLastLogin(userDetails.getId());

        return buildJwtResponse(accessToken, refreshToken, userDetails, roles);
    }

    @Override
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        // 检查用户名是否存在
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null && userMapper.selectByEmail(request.getEmail()) != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        // RSA解密密码
        String rawPassword = rsaUtils.decrypt(request.getPassword());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 分配默认角色
        Role userRole = roleMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                        .eq(Role::getCode, "USER")
        );
        if (userRole != null) {
            userMapper.insertUserRole(user.getId(), userRole.getId());
        }

        // 生成Token
        List<String> roles = Collections.singletonList("USER");
        String accessToken = jwtUtils.generateToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        UserDetailsImpl userDetails = new UserDetailsImpl(user, roles);
        return buildJwtResponse(accessToken, refreshToken, userDetails, roles);
    }

    @Override
    public JwtResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException("无效的刷新令牌");
        }

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String username = jwtUtils.getUsernameFromToken(refreshToken);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 获取用户角色
        List<Role> roles = roleMapper.selectByUserId(userId);
        List<String> roleCodes = roles.isEmpty()
                ? Collections.singletonList("GUEST")
                : roles.stream().map(Role::getCode).toList();

        String accessToken = jwtUtils.generateToken(userId, username, roleCodes);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, username);

        UserDetailsImpl userDetails = new UserDetailsImpl(user, roleCodes);
        return buildJwtResponse(accessToken, newRefreshToken, userDetails, roleCodes);
    }

    @Override
    public UserResponse getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<Role> roles = roleMapper.selectByUserId(userId);
        List<String> roleCodes = roles.stream().map(Role::getCode).toList();

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roleCodes)
                .build();
    }

    @Override
    public void updateLastLogin(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private JwtResponse buildJwtResponse(String accessToken, String refreshToken,
                                          UserDetailsImpl userDetails, List<String> roles) {
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .user(JwtResponse.UserInfo.builder()
                        .id(userDetails.getId())
                        .username(userDetails.getUsername())
                        .email(userDetails.getEmail())
                        .nickname(userDetails.getNickname())
                        .avatar(userDetails.getAvatar())
                        .roles(roles)
                        .build())
                .build();
    }
}