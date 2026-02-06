package com.example.wepai.controller;

import com.example.wepai.data.dto.UserUpdateDTO;
import com.example.wepai.data.vo.Result;
import com.example.wepai.service.UserService;
import jakarta.annotation.Resource;
import com.example.wepai.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;


    static final String DEFAULT_JWT_KEY = "key";




    @GetMapping("/getProfile")
    public ResponseEntity<Result> getProfile(HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return userService.getProfile(casId);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<Result> updateProfile(@RequestBody UserUpdateDTO updateDTO, HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return userService.updateProfile(casId, updateDTO);
    }


    private String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;

        if (token == null) {
            throw new RuntimeException("未提供认证Token");
        }

        com.example.wepai.data.po.User user = JwtUtil.getClaim(token, DEFAULT_JWT_KEY);
        if (user == null) {
            throw new RuntimeException("Token无效或已过期");
        }
        return user.getCasId();
    }
}



