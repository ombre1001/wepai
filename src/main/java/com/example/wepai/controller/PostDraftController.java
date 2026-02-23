package com.example.wepai.controller;

import com.example.wepai.data.dto.PostDTO;
import com.example.wepai.data.po.User;
import com.example.wepai.data.vo.Result;
import com.example.wepai.service.PostService;
import com.example.wepai.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.wepai.controller.UserController.DEFAULT_JWT_KEY;

@CrossOrigin
@RestController
@RequestMapping("/post/draft")
public class PostDraftController {

    @Resource
    private PostService postService;

    /**
     * 保存草稿（新建或更新）
     */
    @PostMapping("/save")
    public ResponseEntity<Result> saveDraft(@RequestBody PostDTO dto, HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return postService.saveDraft(userId, dto);
    }

    /**
     * 获取草稿列表（分页）
     */
    @GetMapping("/list")
    public ResponseEntity<Result> getDraftList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return postService.getDraftList(userId, pageNum, pageSize);
    }

    /**
     * 获取草稿详情
     */
    @GetMapping("/detail")
    public ResponseEntity<Result> getDraftDetail(
            @RequestParam Long postId,
            HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return postService.getDraftDetail(userId, postId);
    }

    /**
     * 删除草稿
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Result> deleteDraft(
            @PathVariable Long postId,
            HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return postService.deleteDraft(userId, postId);
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