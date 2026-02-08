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
@RequestMapping("/order")
@CrossOrigin
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
    public ResponseEntity<Result> getLobbyOrders() {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        query.isNull(Order::getPhotographerId)
                .eq(Order::getStatus, 0)
                .orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.getLobbyOrders();
        return Result.success(orders, "获取大厅订单成功");
    }

    // 获取我的订单
    @GetMapping("/list")
    public ResponseEntity<Result> list(@RequestParam String identity, HttpServletRequest request) {
        String casId = getUserIdFromToken(request);
        return orderService.getMyOrders(casId, identity);
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

    private String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        if (token == null) throw new RuntimeException("未提供认证Token");

        User user = JwtUtil.getClaim(token, DEFAULT_JWT_KEY);
        if (user == null) throw new RuntimeException("Token无效");
        return user.getCasId();
    }
}