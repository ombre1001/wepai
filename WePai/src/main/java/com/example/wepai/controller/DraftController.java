package com.example.wepai.controller;

import com.example.wepai.data.dto.OrderDTO;
import com.example.wepai.data.po.User;
import com.example.wepai.data.vo.Result;
import com.example.wepai.service.OrderService;
import com.example.wepai.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.wepai.controller.UserController.DEFAULT_JWT_KEY;

@RestController
@RequestMapping("/order/draft")
public class DraftController {

    @Resource
    private OrderService orderService;

    @PostMapping("/save")
    public ResponseEntity<Result> saveDraft(@RequestBody OrderDTO dto, HttpServletRequest request) {
        return orderService.saveDraft(getUserIdFromToken(request), dto);
    }



    /**
     * 获取草稿简易列表
     * GET /order/draft/getList?pageNum=1&pageSize=10
     */
    @GetMapping("/getList")
    public ResponseEntity<Result> getList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return orderService.getDraftList(casId, pageNum, pageSize);
    }

    /**
     * 获取草稿详细信息
     * GET /order/draft/getDetail?orderId=123
     */
    @GetMapping("/getDetail")
    public ResponseEntity<Result> getDetail(
            @RequestParam Long orderId,
            HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return orderService.getDraftDetail(casId, orderId);
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        if (token == null) throw new RuntimeException("未提供认证Token");

        User user = JwtUtil.getClaim(token, DEFAULT_JWT_KEY);
        if (user == null) throw new RuntimeException("Token无效");
        return user.getCasId();
    }

    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<Result> deleteDraft(
            @PathVariable Long orderId,
            HttpServletRequest request) {


        String casId = getUserIdFromToken(request);

        return orderService.deleteDraft(casId, orderId);
    }
}
