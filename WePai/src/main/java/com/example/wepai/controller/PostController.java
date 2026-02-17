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

import java.util.Map;

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
    public ResponseEntity<Result> getPosts(
            @RequestParam(required = false) String type, // 改为 String 类型
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return postService.getList(type, pageNum, pageSize);
    }

    @PostMapping("/like/{postId}")
    public ResponseEntity<Result> like(@PathVariable Long postId, HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return postService.likePost(userId, postId);
    }

    // 取消点赞
    @PostMapping("/unlike/{postId}")
    public ResponseEntity<Result> unlike(@PathVariable Long postId, HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        return postService.unlikePost(userId, postId);
    }

    // 评论
    @PostMapping("/comment")
    public ResponseEntity<Result> comment(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        if (!body.containsKey("postId") || !body.containsKey("content")) {
            return Result.error("参数缺失");
        }
        Long postId = Long.valueOf(body.get("postId").toString());
        String content = (String) body.get("content");
        return postService.commentPost(userId, postId, content);
    }

    // 获取评论
    @GetMapping("/comments/{postId}")
    public ResponseEntity<Result> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        return postService.getPostComments(postId, pageNum, pageSize);
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

    @GetMapping("/search")
    public ResponseEntity<Result> search(@RequestParam String keyword, HttpServletRequest request) {
        return postService.searchPosts(getUserIdFromToken(request), keyword);
    }

    @GetMapping("/search/suggest")
    public ResponseEntity<Result> suggest(@RequestParam String keyword) {
        return Result.success(postService.getSuggestions(keyword), "实时建议");
    }

    @GetMapping("/search/history")
    public ResponseEntity<Result> history(HttpServletRequest request) {
        return Result.success(postService.getSearchHistory(getUserIdFromToken(request)), "历史记录");
    }
}
