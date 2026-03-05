package com.example.wepai.controller;

import com.example.wepai.data.po.User;
import com.example.wepai.data.vo.Result; // 假设你有统一返回格式
import com.example.wepai.mapper.UserMapper;
import com.example.wepai.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private UserMapper userMapper;

    // 务必与你系统中的全局统一密钥保持一致
    private static final String DEFAULT_JWT_KEY = "key";

    /**
     * 账号密码登录接口 (专供管理员使用)
     * 接收前端传入的 JSON: {"casId": "Admin", "phone": "123456"}
     */
    @PostMapping("/admin/login")
    public ResponseEntity<Result> adminLogin(@RequestBody Map<String, String> loginData) {
        String casId = loginData.get("casId");
        String phone = loginData.get("phone");

        // 基础校验
        if (casId == null || phone == null) {
            return ResponseEntity.badRequest().body(new Result(400, null, "账号或密码不能为空"));
        }

        // 查询数据库
        User user = userMapper.getUserById(casId);

        // 校验账号存在性与密码(phone)正确性
        if (user == null || !phone.equals(user.getPhone())) {
            return ResponseEntity.status(401).body(new Result(401, null, "账号或密码错误"));
        }

        // 校验是否真的拥有管理员权限
        if (user.getRole() == null || user.getRole() != 0) {
            return ResponseEntity.status(403).body(new Result(403, null, "该账号无管理员权限"));
        }

        //生成包含 role 的 Token
        String token = JwtUtil.generateWithRole(DEFAULT_JWT_KEY, user.getCasId(), user.getName(), user.getRole());

        // 封装返回数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("role", user.getRole()); // 直接返回给前端方便判断，也可以让前端自己解密 token

        return ResponseEntity.ok(new Result(200, responseData, "登录成功"));
    }
}