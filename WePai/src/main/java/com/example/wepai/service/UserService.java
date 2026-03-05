package com.example.wepai.service;

import com.example.wepai.data.dto.FeedbackDTO;
import com.example.wepai.data.dto.UserUpdateDTO;
import com.example.wepai.data.po.Photographer;
import com.example.wepai.data.po.User;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PhotographerMapper photographerMapper;
    @Resource
    private InteractionMapper interactionMapper;
    @Resource
    private SearchMapper searchMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private SystemMapper systemMapper;

    public ResponseEntity<Result> getUserPublicInfo(String targetCasId) {
        User user = userMapper.getUserById(targetCasId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();

        data.put("casId", user.getCasId());
        data.put("nickname", user.getNickname());
        data.put("sex",user.getSex());
        data.put("phone", user.getPhone());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("role", user.getRole());
        data.put("detail", user.getDetail());
        data.put("agreement", user.getAgreement());

        // 获取总获赞量
        int totalLikes = interactionMapper.countTotalLikesReceived(targetCasId);
        data.put("totalLikes", totalLikes);

        data.put("totalOrders", orderMapper.countTotalOrders(targetCasId));
        data.put("completedOrders", orderMapper.countCompletedOrders(targetCasId));


        // 如果是摄影师 (role == 2)，额外获取接单量、风格、评分等
        if (user.getRole() != null && user.getRole() == 2) {
            Photographer pInfo = photographerMapper.getPhotographerById(targetCasId);
            Double avgScore = photographerMapper.getAverageScore(targetCasId);

            if (pInfo != null) {
                data.put("orderCount", pInfo.getOrderCount());
                data.put("style", pInfo.getStyle());
                data.put("equipment", pInfo.getEquipment());
                data.put("photographerType", pInfo.getType());
            } else {
                data.put("orderCount", 0);
            }

            data.put("averageScore", avgScore != null ? avgScore : 0.0);
        }

        return Result.success(data, "获取用户详情成功");
    }


    public ResponseEntity<Result> getProfile(String casId) {
        User user = userMapper.getUserById(casId);
        if (user == null) throw new RuntimeException("用户不存在");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("casId", casId);
        userInfo.put("name", user.getName());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("sex",user.getSex());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("role", user.getRole());
        userInfo.put("detail", user.getDetail());
        userInfo.put("agreement", user.getAgreement());
        int totalLikes = interactionMapper.countTotalLikesReceived(casId);
        userInfo.put("totalLikes", totalLikes);
        userInfo.put("totalOrders", orderMapper.countTotalOrders(casId));
        userInfo.put("completedOrders", orderMapper.countCompletedOrders(casId));

        if (user.getRole() != null && user.getRole() == 2) {
            Photographer pInfo = photographerMapper.getPhotographerById(casId);

            if (pInfo != null) {
                userInfo.put("style", pInfo.getStyle());
                userInfo.put("equipment", pInfo.getEquipment());
                userInfo.put("photographerType", pInfo.getType());
            }
        }

        return Result.success(userInfo, "获取个人信息成功");
    }

    public ResponseEntity<Result> updateProfile(String casId, UserUpdateDTO updateDTO) {
        User existingUser = userMapper.getUserById(casId);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        // --- 核心修改：判断是否有基本信息需要更新 ---
        boolean hasBasicInfo = updateDTO.getNickname() != null ||
                updateDTO.getAvatarUrl() != null ||
                updateDTO.getSex() != null ||
                updateDTO.getPhone() != null ||
                updateDTO.getDetail() != null ||
                updateDTO.getAgreement() != null;

        if (hasBasicInfo) {
            User userToUpdate = new User();
            userToUpdate.setCasId(casId);
            userToUpdate.setNickname(updateDTO.getNickname());
            userToUpdate.setAvatarUrl(updateDTO.getAvatarUrl());
            userToUpdate.setSex(updateDTO.getSex());
            userToUpdate.setPhone(updateDTO.getPhone());
            userToUpdate.setDetail(updateDTO.getDetail());
            userToUpdate.setAgreement(updateDTO.getAgreement());

            userMapper.updateUser(userToUpdate);
        }

        // --- 摄影师扩展信息更新逻辑 ---
        if (existingUser.getRole() == 2 && updateDTO.getPhotographer() != null) {
            UserUpdateDTO.PhotographerDTO pDTO = updateDTO.getPhotographer();
            Photographer p = new Photographer();
            p.setCasId(casId);
            p.setStyle(pDTO.getStyle());
            p.setEquipment(pDTO.getEquipment());
            p.setType(pDTO.getType());

            photographerMapper.upsertPhotographer(p);
        }

        return Result.success(null, "用户信息更新成功");
    }

    boolean isExisted(String userName) {
        // 根据数据库查询用户名是否存在
        Integer count = Integer.valueOf(userMapper.getUserId(userName));
        return count != null && count > 0;
    }

    public ResponseEntity<Result> getAnnouncements() {
        return Result.success(systemMapper.selectActiveAnnouncements(), "获取公告成功");
    }

    public ResponseEntity<Result> submitFeedback(String userId, FeedbackDTO dto) {
        systemMapper.insertFeedback(userId, dto);
        return Result.success(null, "反馈已收到，感谢您的建议");
    }

    public ResponseEntity<Result> getMyFeedbacks(String userId) {
        return Result.success(systemMapper.selectMyFeedbacks(userId), "获取我的反馈成功");
    }


}

