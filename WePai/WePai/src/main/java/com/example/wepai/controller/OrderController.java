package com.example.wepai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wepai.data.dto.OrderDTO;
import com.example.wepai.data.dto.RatingDTO;
import com.example.wepai.data.po.Order;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.OrderMapper;
import com.example.wepai.service.OrderService;
import com.example.wepai.service.UserService;
import com.example.wepai.utils.JwtUtil;
import com.example.wepai.data.po.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.wepai.controller.UserController.DEFAULT_JWT_KEY;

@RestController
@CrossOrigin
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderMapper orderMapper;

    @PostMapping("/create")
    public ResponseEntity<Result> create(@RequestBody OrderDTO dto, HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return orderService.createOrder(casId, dto);
    }

    // 摄影师接单接口
    @PostMapping("/take/{orderId}")
    public ResponseEntity<Result> take(@PathVariable Long orderId, HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return orderService.takeOrder(casId, orderId);
    }

    // 获取广场上的待接订单（大厅模式）
    @GetMapping("/lobby")
    public ResponseEntity<Result> getLobbyOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return orderService.getLobbyOrders(pageNum, pageSize);
    }

    @GetMapping("/accepted")
    public ResponseEntity<Result> getAcceptedOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        // 建议：从 Token 中获取当前登录的摄影师 ID，防止越权查询
        String photographerId = getUserIdFromToken(request);

        return orderService.getAcceptedOrders(photographerId, pageNum, pageSize);
    }

    /**
     * 获取指定订单的评价内容
     * GET /orders/review/detail?orderId=1024
     */
    @GetMapping("/review/detail")
    public ResponseEntity<Result> getReviewDetail(@RequestParam Long orderId) {
        return orderService.getReviewByOrderId(orderId);
    }

    // 获取我的订单
    @GetMapping("/list")
    public ResponseEntity<Result> list(@RequestParam(required = false) Integer status,
                                       @RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       HttpServletRequest request) {

        String userId = getUserIdFromToken(request);
        return orderService.getMyOrders(userId, status, pageNum, pageSize);
    }

    // 订单操作 (接单/拒单/支付/交付)
    @PostMapping("/handle")
    public ResponseEntity<Result> handle(@RequestBody OrderDTO dto, HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return orderService.handleOrder(casId, dto);
    }

    // 评价订单
    @PostMapping("/rate")
    public ResponseEntity<Result> rate(@RequestBody RatingDTO dto, HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return orderService.rateOrder(casId, dto);
    }
    @GetMapping("/photographer/pending")
    public ResponseEntity<Result> getPendingOrders(HttpServletRequest request,@RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        String userId = getUserIdFromToken(request);
        return orderService.getPendingOrders(userId, pageNum, pageSize);
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

    @GetMapping("/works")
    public ResponseEntity<Result> getWorks(
            @RequestParam(required = false) String photographerId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        return orderService.getWorks(photographerId, pageNum, pageSize);
    }

    @GetMapping("/{orderId}/detail")
    public ResponseEntity<Result> getOrderDetail(
            @PathVariable Long orderId) {


        return orderService.getOrderDetail( orderId);
    }

}