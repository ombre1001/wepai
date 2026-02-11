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
@CrossOrigin
@RestController
@RequestMapping("/photographer")
public class PhotographerController {
    @Resource
    private PhotographerService photographerService;




    // 获取所有摄影师列表
    @GetMapping("/list")
    public ResponseEntity<Result> getList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return photographerService.getList(pageNum, pageSize);
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

    @GetMapping("/search")
    public ResponseEntity<Result> search(@RequestParam String keyword, HttpServletRequest request) {
        return photographerService.searchPhotographers(getUserIdFromToken(request), keyword);
    }

    @GetMapping("/search/suggest")
    public ResponseEntity<Result> suggest(@RequestParam String keyword) {
        return Result.success(photographerService.getSuggestions(keyword), "实时建议");
    }

    @GetMapping("/search/history")
    public ResponseEntity<Result> history(HttpServletRequest request) {
        return Result.success(photographerService.getSearchHistory(getUserIdFromToken(request)), "历史记录");
    }

    /**
     * 摄影师接单量排行榜
     * 示例：GET /photographer/ranking/orders?limit=10
     */
    @GetMapping("/ranking/orders")
    public ResponseEntity<Result> getOrderRanking(@RequestParam(required = false) Integer limit) {
        return photographerService.getOrderRanking(limit);
    }

    /**
     * 摄影师评分排行榜
     * 示例：GET /photographer/ranking/ratings?limit=10
     */
    @GetMapping("/ranking/ratings")
    public ResponseEntity<Result> getRatingRanking(@RequestParam(required = false) Integer limit) {
        return photographerService.getRatingRanking(limit);
    }
}
