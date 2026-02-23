package com.example.wepai.data.po;

import com.example.wepai.utils.HttpUtil;
import lombok.SneakyThrows;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CasPageLogin {
    /**
     * 统一认证登入网页
     */
    private static final String CAS_LOGIN_SERVER = "https://pass.sdu.edu.cn/cas/login";

    /**
     * 统一认证ticket校验网页
     */
    private static final String CAS_VALIDATE_SERVER = "https://pass.sdu.edu.cn/cas/serviceValidate";

    /**
     * 本地接口
     */
    public static final String LOCAL_API = "https://i.sdu.edu.cn/cas/proxy/login/page";

    /**
     * 默认跳转
     */
    public static final String DEFAULT_FORWARD = "https://i.sdu.edu.cn/index.html";

    /**
     * 执行cas登入，返回结果
     */
    @SneakyThrows
    public static Result login(String ticket, String forward) {
        if (forward == null) forward = DEFAULT_FORWARD;
        // url编码
        String forwardServerEncoder = URLEncoder.encode(forward, StandardCharsets.UTF_8);
        String localServerEncoder = URLEncoder.encode(LOCAL_API + "?forward=" + forwardServerEncoder, StandardCharsets.UTF_8);

        if (ticket == null) {
            // ticket不存在
            return new Result(false, CAS_LOGIN_SERVER + "?service=" + localServerEncoder, null, null);
        }

        // 检查ticket有效性
        Document xmlDoc = HttpUtil.connect(CAS_VALIDATE_SERVER + "?ticket=" + ticket + "&service=" + localServerEncoder).execute().parse();
        Elements successList = xmlDoc.getElementsByTag("sso:authenticationSuccess");
        if (successList.size() == 1) {
            // ticket有效，获取user信息
            String casID = xmlDoc.getElementsByTag("sso:user").getFirst().text();
            String name = xmlDoc.getElementsByTag("cas:attributes").getFirst().getElementsByTag("cas:user_name").text();
            return new Result(true, forward, casID, name);
        } else {
            // ticket无效，重新进入验证界面
            return new Result(false, CAS_LOGIN_SERVER + "?service=" + localServerEncoder, null, null);
        }
    }

    /**
     * 调用login后返回的结果
     * @param validate 是否校验通过
     * @param redirect 跳转方向
     * @param casId 校验通过后得到的统一认证号
     * @param name 校验通过后得到的名字
     */
    public record Result(boolean validate, String redirect, String casId, String name) {}

}