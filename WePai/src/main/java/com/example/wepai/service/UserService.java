package com.example.wepai.service;

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

    public ResponseEntity<Result> getUserPublicInfo(String targetCasId) {
        User user = userMapper.getUserById(targetCasId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();

        data.put("casId", user.getCasId());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("role", user.getRole());
        data.put("detail", user.getDetail());

        // 获取总获赞量
        int totalLikes = interactionMapper.countTotalLikesReceived(targetCasId);
        data.put("totalLikes", totalLikes);

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
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("role", user.getRole());
        userInfo.put("detail", user.getDetail());
        int totalLikes = interactionMapper.countTotalLikesReceived(casId);
        userInfo.put("totalLikes", totalLikes);
        return Result.success(userInfo, "获取个人信息成功");
    }

    @Transactional
    public ResponseEntity<Result> updateProfile(String casId, UserUpdateDTO updateDTO) {
        User existingUser = userMapper.getUserById(casId);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新主表基本信息
        User userToUpdate = new User();
        userToUpdate.setCasId(casId);
        userToUpdate.setNickname(updateDTO.getNickname());
        userToUpdate.setSex(updateDTO.getSex());
        userToUpdate.setPhone(updateDTO.getPhone());
        userToUpdate.setDetail(updateDTO.getDetail());
        userToUpdate.setAvatarUrl(updateDTO.getAvatarUrl());

        userMapper.updateUser(userToUpdate);

        if (existingUser.getRole() == 2 && updateDTO.getPhotographer() != null) {
            UserUpdateDTO.PhotographerDTO pDTO = updateDTO.getPhotographer();

            Photographer p = new Photographer();
            p.setCasId(casId);
            p.setStyle(pDTO.getStyle());
            p.setEquipment(pDTO.getEquipment());
            p.setType(pDTO.getType());

            // 使用 MyBatis-Plus 的 saveOrUpdate 逻辑（根据 ID 判断是 Insert 还是 Update）
            // 如果你使用的是原生 Mapper，建议先 select 再决定 update 还是 insert
            try {
                // 使用原生方法进行判断
                photographerMapper.upsertPhotographer(p);
            } catch (Exception e) {
                e.printStackTrace(); // 必须打印出来看具体的 SQL 错误
                return Result.error("操作扩展表失败：" + e.getMessage());
            }
        }

        // 如果是摄影师且传了相关属性，则更新摄影师表

        return Result.success(userToUpdate, "用户信息更新成功");
    }

    boolean isExisted(String userName) {
        // 根据数据库查询用户名是否存在
        Integer count = Integer.valueOf(userMapper.getUserId(userName));
        return count != null && count > 0;
    }


}

