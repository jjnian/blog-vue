package com.jinian.blog.common.constant;

/**
 * 安全相关常量
 */
public class SecurityConstants {

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EDITOR = "EDITOR";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_GUEST = "GUEST";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLES = "roles";

    private SecurityConstants() {}
}