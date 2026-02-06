package com.example.wepai.controller;

import com.example.wepai.data.po.CasPageLogin;
import com.example.wepai.data.po.User;
import com.example.wepai.utils.JwtUtil;
import com.example.wepai.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * 使用统一认证页面实现统一认证登入
 */

@Controller
public class CasPageLoginController {
    @Resource
    private UserMapper userMapper;

    public static final HashMap<String, String> allowApiTable = new HashMap<>();

    static {
        // 跳转站点api - 站点密钥key
        allowApiTable.put("http://127.0.0.1:8080/login", "key");
        //allowApiTable.put("https://i.sdu.edu.cn/cas/proxy/login", "key1");  // 统一认证页面方式登入后端示例api
    }

    /**
     * 统一认证登入页面重定向代理接口
     *
     * @param ticket 统一认证登入发放的ticket
     * @param forward 统一认证登入成功后的回调接口
     */
    @SneakyThrows
    @RequestMapping("/login/page")
    public void casLogin(
            @RequestParam(defaultValue = CasPageLogin.DEFAULT_FORWARD) String forward,
            @RequestParam(required = false) String ticket,
            HttpServletResponse response) {
        // 根据重定向api获取该api设定的加密jwt的key
        String key = allowApiTable.get(forward);

        // 如果key==null，说明该重定向网站没注册过，拒绝统一认证登入请求


        // 统一认证登入检查
        CasPageLogin.Result result = CasPageLogin.login(ticket, forward);

        // 如果ticket有效
        if (result.validate()) {
            String token = JwtUtil.generate(key, result.casId(), result.name());
            // 登入成功，在重定向api后面拼接上token
            response.sendRedirect(result.redirect() + (result.redirect().contains("?") ? "&token=" : "?token=") + token);
        } else {
            // 直接重定向
            response.sendRedirect(result.redirect());
        }

    }


    /**
     * 统一认证页面方式登入后端示例回调接口
     * @param token 统一认证登入页面重定向代理接口给出的token
     */
    @ResponseBody
    @SneakyThrows
    @RequestMapping("/login")
    public String login(@RequestParam String token, HttpServletResponse response) {
        // 存储在服务器上的key应该和存储在服务器上的key相同
        String key = "key";

        User user = JwtUtil.getClaim(token, key);
        // 如果解密token失败，那么这是一个非法的登入
        if (user == null) {
            // 非法登入逻辑处理
            return "登入失败，token不合法！";
        }
        // 如果解密token成功，那么这是一个合法的登入
        else {
            if (userMapper.getUserById(user.getCasId()) == null) {
                // 执行初始化注册
                userMapper.insertUser(user);
            }
            // 合法登入逻辑处理
            String casID = URLEncoder.encode(user.getCasId(), StandardCharsets.UTF_8);
            String name = URLEncoder.encode(user.getName(), StandardCharsets.UTF_8);
            response.sendRedirect("http://localhost:8080/index.html?token=" + token);

            return null;
        }
    }

}
