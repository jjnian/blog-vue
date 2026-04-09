package com.jinian.blog.config;

import com.jinian.blog.security.JwtAuthenticationEntryPoint;
import com.jinian.blog.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(new CorsConfig().corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 认证
                        .requestMatchers("/auth/login", "/auth/register", "/auth/refresh").permitAll()
                        // 公开接口 - 文章（GET公开，POST/PUT/DELETE需要认证）
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/articles", "/articles/**").permitAll()
                        // 公开接口 - 分类、标签
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/categories", "/categories/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/tags", "/tags/**").permitAll()
                        // 公开接口 - 留言、许愿、友链
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/messages").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/messages").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/wishes").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/wishes").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/links").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/comments", "/comments/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/comments").permitAll()
                        // Swagger文档
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 管理接口 - 需要ADMIN角色
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // 其他接口需要认证（包括POST/PUT/DELETE文章）
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}