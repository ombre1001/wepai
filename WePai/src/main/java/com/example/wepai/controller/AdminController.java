package com.example.wepai.controller;

import com.example.wepai.data.dto.AnnouncementDTO;
import com.example.wepai.data.vo.Result;
import com.example.wepai.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

    @Resource
    private AdminService adminService;

    // ==================== 用户管理 ====================

    // 分页获取所有用户列表
    @GetMapping("/users")
    public ResponseEntity<Result> getAllUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return adminService.getAllUsers(pageNum, pageSize);
    }

    // 封禁/解封用户 (status: 0正常, 1封禁)
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Result> updateUserStatus(
            @PathVariable String userId,
            @RequestParam Integer status) {
        return adminService.updateUserStatus(userId, status);
    }

    // ==================== 内容管理 (帖子 & 评论) ====================

    // 分页获取所有帖子（供审核）
    @GetMapping("/posts")
    public ResponseEntity<Result> getAllPosts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return adminService.getAllPosts(pageNum, pageSize);
    }

    // 管理员强制删除违规帖子
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Result> deletePostForce(@PathVariable Long postId) {
        return adminService.deletePostForce(postId);
    }

    // 管理员强制删除违规评论
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Result> deleteCommentForce(@PathVariable Long commentId) {
        return adminService.deleteCommentForce(commentId);
    }

    // ==================== 订单管理 ====================

    // 查看平台所有订单
    @GetMapping("/orders")
    public ResponseEntity<Result> getAllOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return adminService.getAllOrders(pageNum, pageSize);
    }

    @PostMapping("/announcements")
    public ResponseEntity<Result> publish(@RequestBody AnnouncementDTO dto) {
        return adminService.publishAnnouncement(dto);
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<Result> getFeedbacks(@RequestParam(defaultValue = "1") int pageNum) {
        return adminService.getAllFeedbacks(pageNum, 10);
    }

    @PostMapping("/feedback/read")
    public ResponseEntity<Result> markRead(@RequestParam Long feedbackId) {
        return adminService.markFeedbackAsRead(feedbackId);
    }

    @PostMapping("/photographers/revoke")
    public ResponseEntity<Result> revokePhotographer(
            @RequestParam String casId,
            @RequestParam String reason) {
        return adminService.cancelPhotographer(casId, reason);
    }

    @GetMapping("/comments")
    public ResponseEntity<Result> getAllComments(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return adminService.getAllComments(pageNum, pageSize);
    }
}