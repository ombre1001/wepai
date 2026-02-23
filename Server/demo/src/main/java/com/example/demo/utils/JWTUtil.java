package com.example.demo.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



@Component
public class JWTUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.refresh-secret-key}")
    private String refreshSecretKey;

    public static String SECRET_KEY;
    public static String REFRESH_SECRET_KEY;

    // 初始化静态变量
    @PostConstruct
    public void init() {
        SECRET_KEY = secretKey;
        REFRESH_SECRET_KEY = refreshSecretKey;
    }

    public static final int EXPIRE_TIME = 1800;//Token过期时间
    public static final int REFRESH_EXPIRE_TIME = 2 * 60 *60;//RefreshToken过期时间

    public String generateToken(String accountId) {
        return getToken(accountId, EXPIRE_TIME, SECRET_KEY);
    }
    //生成token
    public String getToken(String accountId, int expireTime, String key) {
        try {
            // 参数校验
            validateParameters(accountId, expireTime, key);

            // 确保key长度足够
            String secureKey = ensureKeyLength(key);

            Map<String, String> map = new HashMap<>();
            map.put("user_id", accountId);

            Date expiresAt = new Date(System.currentTimeMillis() + expireTime * 1000L);

            return JWT.create()
                    .withClaim("user_id", accountId)
                    .withExpiresAt(expiresAt)
                    .sign(Algorithm.HMAC256(secureKey));

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("参数错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("生成Token失败: " + e.getMessage(), e);
        }
    }
    public boolean validateToken(String token) {
        return validateToken(token, SECRET_KEY);
    }

    public boolean validateToken(String token, String key) {
        try {
            String secureKey = ensureKeyLength(key);
            JWT.require(Algorithm.HMAC256(secureKey)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateParameters(String accountId, int expireTime, String key) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("accountId不能为空");
        }
        if (expireTime <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }
    }

    private String ensureKeyLength(String key) {
        // 如果key长度不足，进行填充
        if (key.length() < 32) {
            return String.format("%-32s", key).substring(0, 32);
        }
        return key;
    }



    // 获取用户 ID
    public String getUserId(String token) {
        return getUserId(token, SECRET_KEY);
    }

    // 获取用户 ID（自定义密钥）
    public String getUserId(String token, String key) {
        try {
            validateToken(token, key);

            String secureKey = ensureKeyLength(key);
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secureKey))
                    .build()
                    .verify(token);

            return decodedJWT.getClaim("user_id").asString();

        } catch (TokenExpiredException e) {
            throw new RuntimeException("Token 已过期，请重新登录");
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token 无效，请检查后重试");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Token 无效，请检查后重试");
        } catch (Exception e) {
            throw new RuntimeException("解析 Token 时发生错误，请稍后重试");
        }
    }

}
