package com.example.wepai.controller;

import com.example.wepai.data.dto.PostDTO;
import com.example.wepai.data.vo.Result;
import com.example.wepai.service.PostService;
import com.example.wepai.utils.JwtUtil;
import com.example.wepai.data.po.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.wepai.controller.UserController.DEFAULT_JWT_KEY;

@RestController
@RequestMapping("/square")
@CrossOrigin
public class PostController {

    @Resource
    private PostService postService;

    // 发布帖子
    @PostMapping("/publish")
    public ResponseEntity<Result> publish(@RequestBody PostDTO dto, HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return postService.publish(casId, dto);
    }

    // 获取帖子列表 (1-需求, 2-作品)
    @GetMapping("/posts")
    public ResponseEntity<Result> getPosts(@RequestParam(required = false) Integer type) {
        return postService.getList(type);
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        if (token == null) throw new RuntimeException("未提供认证Token");
        User user = JwtUtil.getClaim(token, DEFAULT_JWT_KEY);
        if (user == null) throw new RuntimeException("Token无效");
        return user.getCasId();
    }
}
