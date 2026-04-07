package com.jinian.blog.security;

import com.jinian.blog.entity.Role;
import com.jinian.blog.entity.User;
import com.jinian.blog.mapper.RoleMapper;
import com.jinian.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户详情服务实现
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 获取用户角色
        List<Role> roles = roleMapper.selectByUserId(user.getId());
        List<String> roleCodes = roles.isEmpty()
                ? Collections.singletonList("GUEST")
                : roles.stream().map(Role::getCode).toList();

        return new UserDetailsImpl(user, roleCodes);
    }
}