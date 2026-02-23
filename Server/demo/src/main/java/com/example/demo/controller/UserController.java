package com.example.demo.controller;

import com.example.demo.data.dto.AuthDTO;
import com.example.demo.data.dto.PasswordChangeDTO;
import com.example.demo.data.dto.UserUpdateDTO;
import com.example.demo.data.vo.Result;
import com.example.demo.service.UserService;
import com.example.demo.utils.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private JWTUtil jwtUtil;


    @GetMapping("/getProfile")
    public ResponseEntity<Result> getProfile(HttpServletRequest request) {
            String userId = getUserIdFromToken(request);
            return userService.getProfile(userId);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<Result> updateProfile(@RequestBody UserUpdateDTO updateDTO,HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return userService.updateProfile(userId,updateDTO);
    }

    @PutMapping("/updatePassword")
    public ResponseEntity<Result> updatePassword(@RequestBody PasswordChangeDTO passwordDTO, HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return userService.updatePassword(userId,passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<Result> deleteAccount(HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return userService.deleteAccount(userId);
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new RuntimeException("缺少Authorization头部信息");
        }

        // 处理Bearer token格式
        String token;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            token = authHeader;
        }

        if (token.trim().isEmpty()) {
            throw new RuntimeException("Token不能为空");
        }

        return jwtUtil.getUserId(token);
    }
}



