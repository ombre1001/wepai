package com.example.wepai.controller;

import com.example.wepai.data.vo.Result;
import com.example.wepai.service.PhotographerService;
import com.example.wepai.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.wepai.controller.UserController.DEFAULT_JWT_KEY;

// PhotographerController.java
@RestController
@RequestMapping("/photographer")
public class PhotographerController {
    @Resource
    private PhotographerService photographerService;

    // 获取自身或指定摄影师的评分和接单量
    @GetMapping("/performance/{casId}")
    public ResponseEntity<Result> getPerformance(@PathVariable String casId) {
        return photographerService.getPerformance(casId);
    }

    // 获取详情属性
    @GetMapping("/attributes/{casId}")
    public ResponseEntity<Result> getAttributes(@PathVariable String casId) {
        return photographerService.getUniqueAttributes(casId);
    }

    // 获取所有摄影师列表
    @GetMapping("/list")
    public ResponseEntity<Result> getList() {
        return photographerService.getList();
    }
    @PostMapping("/enroll")
    public ResponseEntity<Result> enroll(@RequestParam String inviteCode, HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return photographerService.enroll(casId, inviteCode);
    }

    // 复用您的 Token 解析逻辑
    private String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;

        if (token == null) {
            throw new RuntimeException("未提供认证Token");
        }

        // 假设 JwtUtil.getClaim 返回的是 User 对象或包含 ID 的对象
        com.example.wepai.data.po.User user = JwtUtil.getClaim(token, DEFAULT_JWT_KEY);
        if (user == null) {
            throw new RuntimeException("Token无效");
        }
        return user.getCasId();
    }
}
