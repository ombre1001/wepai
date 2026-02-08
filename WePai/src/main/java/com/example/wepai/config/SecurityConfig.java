package com.example.wepai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF（前后端分离项目通常关闭它，否则 POST 请求会被拦截）
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()    // 允许所有人访问 /login
                        .requestMatchers("/register").permitAll() // 如果有注册接口也放开
                        .anyRequest().authenticated()             // 其他所有接口必须登录（携带 Token）后访问
                );

        return http.build();
    }
}