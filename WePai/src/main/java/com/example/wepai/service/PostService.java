package com.example.wepai.service;

import com.example.wepai.data.dto.PostDTO;
import com.example.wepai.data.po.Post;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.PostMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostService {

    @Resource
    private PostMapper postMapper;

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
}