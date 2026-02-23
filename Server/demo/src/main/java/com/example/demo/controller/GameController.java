package com.example.demo.controller;

import com.example.demo.data.vo.Result;
import com.example.demo.service.GameService;
import com.example.demo.utils.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/game")
public class GameController {

    @Resource
    private GameService gameService;


    @Resource
    private JWTUtil jwtUtil;

    /**
     * 获取游戏列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getGameList() {
        return gameService.getAllGames();
    }




    /**
     * 获取用户游戏历史记录
     */
    @GetMapping("/history")
    public ResponseEntity<Result> getUserGameHistory(HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return gameService.getUserGameHistory(Integer.valueOf(userId));
    }

    /**
     * 清空用户游戏历史记录
     */
    @DeleteMapping("/history/clear")
    public ResponseEntity<Result> clearUserGameHistory(HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return gameService.clearUserGameHistory(Integer.valueOf(userId));
    }

    /**
     * 获取用户指定游戏的最高分记录
     */
    @GetMapping("/history/best/{gameId}")
    public ResponseEntity<Result> getUserBestScore(@PathVariable Integer gameId,
                                                   HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        // 根据您选择的方案调用相应的方法
        return gameService.getUserBestScore(Integer.valueOf(userId), gameId);
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
