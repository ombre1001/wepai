package com.example.demo.controller;

import com.example.demo.data.vo.Result;
import com.example.demo.service.Game2048Service;
import com.example.demo.utils.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/game/2048")
public class Game2048Controller {

    @Resource
    private Game2048Service game2048Service;

    @Resource
    private JWTUtil jwtUtil;

    @PostMapping("/start")
    public ResponseEntity<Result> start(HttpServletRequest request) {
        return game2048Service.startNewGame(getUserIdFromToken(request));
    }

    @PostMapping("/move")
    public ResponseEntity<Result> move(@RequestBody Map<String, String> request,
                                       HttpServletRequest httpRequest) {
        String direction = request.get("direction");
        return game2048Service.move(getUserIdFromToken(httpRequest), direction);
    }

    @PostMapping("/restart")
    public ResponseEntity<Result> restart(HttpServletRequest request) {
        return game2048Service.restart(getUserIdFromToken(request));
    }

    @GetMapping("/status")
    public ResponseEntity<Result> status(HttpServletRequest request) {
        return game2048Service.getStatus(getUserIdFromToken(request));
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