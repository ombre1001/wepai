package com.example.wepai.service;

import com.example.wepai.data.po.Photographer;
import com.example.wepai.data.po.User;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.PhotographerMapper;
import com.example.wepai.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

// PhotographerService.java
@Service
public class PhotographerService {
    @Resource
    private PhotographerMapper photographerMapper;
    @Resource
    private UserMapper userMapper;

    // 获取评分和接单量
    public ResponseEntity<Result> getPerformance(String casId) {
        Double avgScore = photographerMapper.getAverageScore(casId);
        Photographer info = photographerMapper.getPhotographerById(casId);

        Map<String, Object> data = new HashMap<>();
        data.put("averageScore", avgScore != null ? avgScore : 0.0);
        data.put("orderCount", info != null ? info.getOrderCount() : 0);

        return Result.success(data, "获取评分和接单量成功");
    }

    // 获取摄影师独特属性
    public ResponseEntity<Result> getUniqueAttributes(String casId) {
        Photographer info = photographerMapper.getPhotographerById(casId);
        if (info == null) throw new RuntimeException("未找到摄影师信息");

        Map<String, String> data = new HashMap<>();
        data.put("style", info.getStyle());
        data.put("equipment", info.getEquipment());
        data.put("type", info.getType());

        return Result.success(data, "获取属性成功");
    }

    // 获取摄影师列表
    public ResponseEntity<Result> getList() {
        return Result.success(photographerMapper.getPhotographerList(), "获取列表成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> enroll(String casId, String inviteCode) {
        String validCode = "SDU_PHOTO_666";
        if (!validCode.equals(inviteCode)) {
            return Result.error("验证码不正确");
        }

        try {
            User user = userMapper.getUserById(casId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            int affectedRows = userMapper.updateUserRole(casId, 2);
            if (affectedRows <= 0) {
                return Result.error("更新用户角色失败");
            }

            Photographer info = photographerMapper.getPhotographerById(casId);

            if (info == null) {
                Photographer newInfo = new Photographer();
                newInfo.setCasId(casId);
                newInfo.setOrderCount(0);
                photographerMapper.insertPhotographer(newInfo);
            }

            return Result.success(null, "恭喜，入驻成功！已获得摄影师权限。");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("数据库操作失败: " + e.getMessage());
        }
    }

}