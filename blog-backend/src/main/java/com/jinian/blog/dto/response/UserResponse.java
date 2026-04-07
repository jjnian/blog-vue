package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private List<String> roles;
}