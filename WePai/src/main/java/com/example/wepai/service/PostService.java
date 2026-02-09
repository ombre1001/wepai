package com.example.wepai.service;

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
import java.util.List;

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
        return Result.success(null, "发布成功");
    }

    public ResponseEntity<Result> getList(Integer type) {
        return Result.success(postMapper.selectPostsWithUser(type), "获取广场列表成功");
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
        return Result.success(null, "评论成功");
    }

    // 获取评论
    public ResponseEntity<Result> getPostComments(Long postId) {
        return Result.success(interactionMapper.getCommentsByPostId(postId), "获取评论列表成功");
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