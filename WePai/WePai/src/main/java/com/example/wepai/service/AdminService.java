package com.example.wepai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.dto.AnnouncementDTO;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private InteractionMapper interactionMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private SystemMapper systemMapper;
    @Resource
    private PhotographerMapper photographerMapper;

    // 获取所有用户
    public ResponseEntity<Result> getAllUsers(int pageNum, int pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        List<Map<String, Object>> list = userMapper.selectAllUsers(page);
        return Result.success(buildPageData(list, page), "获取用户列表成功");
    }

    // 更新用户状态
    public ResponseEntity<Result> updateUserStatus(String userId, Integer status) {
        int rows = userMapper.updateUserStatus(userId, status);
        return rows > 0 ? Result.success(null, "状态更新成功") : Result.error("更新失败或用户不存在");
    }

    // 获取所有帖子
    public ResponseEntity<Result> getAllPosts(int pageNum, int pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        List<Map<String, Object>> list = postMapper.selectAllPostsAdmin(page);
        return Result.success(buildPageData(list, page), "获取帖子列表成功");
    }

    // 强制删除帖子
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> deletePostForce(Long postId) {
        int rows = postMapper.deletePostForce(postId);
        return rows > 0 ? Result.success(null, "帖子已强制删除") : Result.error("帖子不存在");
    }

    // 强制删除评论
    public ResponseEntity<Result> deleteCommentForce(Long commentId) {
        int rows = interactionMapper.deleteCommentForce(commentId);
        return rows > 0 ? Result.success(null, "评论已强制删除") : Result.error("评论不存在");
    }

    // 获取所有订单
    public ResponseEntity<Result> getAllOrders(int pageNum, int pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        List<Map<String, Object>> list = orderMapper.selectAllOrdersAdmin(page);
        return Result.success(buildPageData(list, page), "获取订单列表成功");
    }

    // 统一封装分页返回数据
    private Map<String, Object> buildPageData(List<Map<String, Object>> list, Page<?> page) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());
        return data;
    }

    // 发布公告
    public ResponseEntity<Result> publishAnnouncement(AnnouncementDTO dto) {
        systemMapper.insertAnnouncement(dto);
        return Result.success(null, "公告发布成功");
    }

    // 获取所有用户反馈
    public ResponseEntity<Result> getAllFeedbacks(int pageNum, int pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        List<Map<String, Object>> list = systemMapper.selectAllFeedbacks(page);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        return Result.success(data, "获取反馈成功");
    }

    public ResponseEntity<Result> markFeedbackAsRead(Long feedbackId) {
        int rows = systemMapper.markAsRead(feedbackId);
        if (rows > 0) {
            return Result.success(null, "已标记为已读");
        }
        return Result.error("操作失败，反馈ID可能不存在");
    }

    // 取消摄影师身份
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> cancelPhotographer(String casId, String reason) {
        // 1. 修改用户角色为普通用户
        systemMapper.updateRoleToUser(casId);
        // 2. 更新摄影师表状态并记录原因
        photographerMapper.deletePhotographerRecord(casId);
        String msg = String.format("已取消身份。原因：%s", reason);
        return Result.success(null, msg);
    }

    public ResponseEntity<Result> getAllComments(int pageNum, int pageSize) {
        // 1. 创建分页对象
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);

        // 2. 执行查询
        List<Map<String, Object>> list = interactionMapper.selectAllCommentsAdmin(page);

        // 3. 封装分页结果
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());
        data.put("currentPage", page.getCurrent());

        return Result.success(data, "获取全平台评论成功");
    }
}