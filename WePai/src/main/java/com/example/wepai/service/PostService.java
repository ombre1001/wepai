package com.example.wepai.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.dto.PostDTO;
import com.example.wepai.data.dto.PostDraftListDTO;
import com.example.wepai.data.po.Post;
import com.example.wepai.data.po.PostComment;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.InteractionMapper;
import com.example.wepai.mapper.PostMapper;
import com.example.wepai.mapper.SearchMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> saveDraft(String userId, PostDTO dto) {
        Post post;
        if (dto.getPostId() != null) {
            // 更新已有草稿：必须属于当前用户且状态为草稿
            post = postMapper.selectPostDraftById(dto.getPostId(), userId);
            if (post == null) {
                return Result.error("草稿不存在或无权操作");
            }
        } else {
            // 新建草稿，检查数量限制
            Page<PostDraftListDTO> countPage = new Page<>(1, 3);
            List<PostDraftListDTO> existingDrafts = postMapper.selectDraftListPaged(countPage, userId);
            if (existingDrafts != null && existingDrafts.size() >= 3) {
                return Result.error("草稿箱已满（最多存储" + 3 + "个），请删除部分草稿后再试");
            }
            post = new Post();
            post.setUserId(userId);
            post.setCreatedAt(LocalDateTime.now());
        }

        // 填充DTO数据
        copyDtoToPost(dto, post);
        post.setStatus(-1);           // 确保状态为草稿
        // 更新时间（如果更新操作需要刷新创建时间，可根据需求决定是否更新）
        // 例如：post.setCreatedAt(LocalDateTime.now()); // 若需要更新时间则放开

        if (post.getPostId() != null) {
            postMapper.updateById(post);         // MyBatis-Plus 通用 update
        } else {
            postMapper.insertPost(post);
        }

        Map<String, Object> resMap = new HashMap<>();
        resMap.put("postId", post.getPostId());
        resMap.put("savedAt", post.getCreatedAt());
        return Result.success(resMap, "草稿保存成功");
    }

    /**
     * 获取草稿列表
     */
    public ResponseEntity<Result> getDraftList(String userId, int pageNum, int pageSize) {
        Page<PostDraftListDTO> page = new Page<>(pageNum, pageSize);
        List<PostDraftListDTO> list = postMapper.selectDraftListPaged(page, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());
        return Result.success(data, "获取草稿列表成功");
    }

    /**
     * 获取草稿详情
     */
    public ResponseEntity<Result> getDraftDetail(String userId, Long postId) {
        Post post = postMapper.selectPostDraftById(postId, userId);
        if (post == null) {
            return Result.error("草稿不存在或无权查看");
        }
        // 将 images 转为 List（如果前端需要数组格式）
        // 注意：由于 selectPostDraftById 已配置 TypeHandler，post.getImages() 已经是 List<String>
        return Result.success(post, "获取草稿详情成功");
    }

    /**
     * 删除草稿
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> deleteDraft(String userId, Long postId) {
        int rows = postMapper.deleteDraftManual(postId, userId);
        if (rows > 0) {
            return Result.success(null, "草稿删除成功");
        } else {
            return Result.error("删除失败，草稿不存在或无权操作");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> publish(String userId, PostDTO dto) {
        Post post;
        if (dto.getPostId() != null) {
            // 从草稿发布：查询草稿
            post = postMapper.selectPostDraftById(dto.getPostId(), userId);
            if (post == null) {
                return Result.error("草稿不存在或无权操作");
            }
        } else {
            // 新建帖子
            post = new Post();
            post.setUserId(userId);
            post.setCreatedAt(LocalDateTime.now());
        }

        // 填充数据
        copyDtoToPost(dto, post);
        post.setStatus(1);        // 发布状态

        if (post.getPostId() != null) {
            postMapper.updateById(post);
        } else {
            postMapper.insertPost(post);
        }

        Map<String, Object> resMap = new HashMap<>();
        resMap.put("postId", post.getPostId());
        resMap.put("createTime", post.getCreatedAt());
        return Result.success(resMap, "发布成功");
    }

    private void copyDtoToPost(PostDTO dto, Post post) {
        post.setType(dto.getType());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());

    }


    public ResponseEntity<Result> getList(Integer type, int pageNum, int pageSize, String currentUserId, String keyword) {
        // 1. 处理空字符串：如果前端传了 "" 或全空格，统一转为 null，让 SQL 查全部
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        // 2. 记录搜索历史（只有当 keyword 存在，且用户已登录时才记录）
        if (keyword != null && currentUserId != null) {
            searchMapper.insertHistory(currentUserId, keyword, "post");
        }

        // 3. 分页查询
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        // 调用修改后的 Mapper 方法
        List<Map<String, Object>> list = postMapper.selectPostsSimplified(page, type, currentUserId, keyword);

        // 4. 处理图片 JSON 数组转单图
        list.forEach(item -> {
            String imagesStr = (String) item.get("images");
            if (imagesStr != null && imagesStr.startsWith("[")) {
                List<String> imgList = JSONUtil.toList(imagesStr, String.class);
                item.put("images", imgList.isEmpty() ? "" : imgList.get(0));
            }
        });

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        return Result.success(data, "获取广场列表成功");
    }

    /**
     * 获取帖子详情
     */
    public ResponseEntity<Result> getPostDetail(Long postId, String currentUserId) {
        Map<String, Object> detail = postMapper.selectPostDetail(postId, currentUserId);
        if (detail == null) return Result.error("帖子不存在");

        // 详情页 images 转为数组
        String imagesStr = (String) detail.get("images");
        if (imagesStr != null && imagesStr.startsWith("[")) {
            detail.put("images", JSONUtil.toList(imagesStr, String.class));
        } else {
            detail.put("images", new String[]{imagesStr});
        }

        return Result.success(detail, "获取详情成功");
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


    public List<String> getSuggestions(String keyword) {
        return postMapper.getSuggestions(keyword);
    }

    public List<String> getSearchHistory(String userId) {
        return searchMapper.getHistory(userId, "post");
    }

    public ResponseEntity<Result> getMyPosts(String userId, int pageNum, int pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        List<Map<String, Object>> list = postMapper.selectMyPosts(page, userId);


        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());

        return Result.success(data, "获取个人帖子成功");
    }
}