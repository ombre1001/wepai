package com.example.wepai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.dto.PostDTO;
import com.example.wepai.data.po.Post;
import com.example.wepai.data.po.PostComment;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.InteractionMapper;
import com.example.wepai.mapper.PostMapper;
import com.example.wepai.mapper.SearchMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    @Resource
    private PostMapper postMapper;

    @Resource
    private InteractionMapper interactionMapper;

    @Resource
    private SearchMapper searchMapper;

    public ResponseEntity<Result> publish(String userId, PostDTO dto) {
        Post post = new Post();
        post.setUserId(userId);
        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        post.setStatus(1);
        post.setCreatedAt(LocalDateTime.now());

        postMapper.insertPost(post);
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("postId", post.getPostId()); // 确保 Post 实体类中有 getPostId()
        resMap.put("createTime", post.getCreatedAt());

        return Result.success(resMap, "发布成功");
    }

    public ResponseEntity<Result> getList(String type, int pageNum, int pageSize) { // 参数改为 String
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        List<Map<String, Object>> list = postMapper.selectPostsWithUserPaged(page, type);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());

        return Result.success(data, "获取广场列表成功");
    }

    public ResponseEntity<Result> likePost(String userId, Long postId) {
        try {
            interactionMapper.insertLike(postId, userId);
            return Result.success(null, "点赞成功");
        } catch (Exception e) {
            return Result.error("已点赞或操作失败");
        }
    }

    // 取消点赞
    public ResponseEntity<Result> unlikePost(String userId, Long postId) {
        interactionMapper.deleteLike(postId, userId);
        return Result.success(null, "取消点赞成功");
    }

    // 评论
    public ResponseEntity<Result> commentPost(String userId, Long postId, String content) {
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        interactionMapper.insertComment(comment);
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("commentId", comment.getId()); // 对应 PostComment.java 中的 id 字段
        resMap.put("createTime", comment.getCreatedAt());

        return Result.success(resMap, "评论成功");
    }

    // 获取评论
    public ResponseEntity<Result> getPostComments(Long postId, int pageNum, int pageSize) {
        // 1. 创建分页对象
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);

        // 2. 查询数据库 (MyBatis-Plus 会自动将 total 填入 page 对象)
        List<Map<String, Object>> list = interactionMapper.selectCommentsByPostIdPaged(page, postId);

        // 3. 封装返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);            // 评论数据列表
        data.put("total", page.getTotal());  // 总评论数
        data.put("pages", page.getPages());  // 总页数

        return Result.success(data, "获取评论列表成功");
    }

    public ResponseEntity<Result> searchPosts(String userId, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            searchMapper.insertHistory(userId, keyword, "post");
        }
        return Result.success(postMapper.searchPosts(keyword), "搜索完成");
    }

    public List<String> getSuggestions(String keyword) {
        return postMapper.getSuggestions(keyword);
    }

    public List<String> getSearchHistory(String userId) {
        return searchMapper.getHistory(userId, "post");
    }
}