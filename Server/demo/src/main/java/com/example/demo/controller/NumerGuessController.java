package com.example.demo.controller;

import com.example.demo.data.vo.Result;
import com.example.demo.service.Game2048Service;
import com.example.demo.service.NumberGuessService;
import com.example.demo.utils.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/game/numberGuess")
public class NumerGuessController {
    @Resource
    private NumberGuessService numberGuessService;

    @Resource
    private JWTUtil jwtUtil;

    @PostMapping("/start")
    public ResponseEntity<Result> start(HttpServletRequest request) {
    String userId = getUserIdFromToken(request);
    return numberGuessService.startGame(userId);
    }

    /**
     * 猜数字
     */
    @PostMapping("/guess")
    public ResponseEntity<Result> guessNumber(@RequestBody Map<String, Integer> requestBody,
                                              HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        Integer guess = requestBody.get("guess");
        return numberGuessService.guessNumber(userId, guess);
    }

    /**
     * 获取猜数字游戏状态
     */
    @GetMapping("/status")
    public ResponseEntity<Result> getNumberGuessingStatus(HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return numberGuessService.getGameStatus(userId);
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
