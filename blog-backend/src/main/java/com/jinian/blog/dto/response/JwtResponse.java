package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * JWT响应
 */
@Data
@Builder
public class JwtResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private List<String> roles;
        /** 用户有效功能权限码列表 */
        private List<String> permissions;
    }
}