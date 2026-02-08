package com.example.wepai.service;

import com.example.wepai.data.dto.OrderDTO;
import com.example.wepai.data.dto.RatingDTO;
import com.example.wepai.data.po.Order;
import com.example.wepai.data.po.Photographer;
import com.example.wepai.data.po.Rating;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.OrderMapper;
import com.example.wepai.mapper.PhotographerMapper;
import com.example.wepai.mapper.RatingMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RatingMapper ratingMapper;
    @Resource
    private PhotographerMapper photographerMapper;

    // 创建订单
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> createOrder(String customerId, OrderDTO dto) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setType(dto.getType());
        order.setShootTime(dto.getShootTime());
        order.setLocation(dto.getLocation());
        order.setPrice(dto.getPrice());
        order.setRemark(dto.getRemark());
        order.setCreatedAt(LocalDateTime.now());

        // 校验摄影师 ID
        String pId = dto.getPhotographerId();
        if (pId != null && !pId.trim().isEmpty()) {
            // 指定了摄影师 -> 必须校验其是否存在于摄影师表
            Photographer p = photographerMapper.getPhotographerById(pId);
            if (p == null) {
                return Result.error("指定的摄影师未入驻或不存在");
            }
            order.setPhotographerId(pId);
            order.setStatus(1); // 设为已接单/待支付
        } else {
            // 未指定摄影师 -> 进入广场池
            order.setPhotographerId(null);
            order.setStatus(0); // 设为广场待领取
        }

        try {
            orderMapper.insertOrder(order);
            // 因为加了 @Options，执行完 insert 后，order.getOrderId() 会自动获得数据库生成的值
            return Result.success(order.getOrderId(), "订单发起成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("订单发起失败：" + e.getMessage());
        }
    }

    /**
     * 摄影师接单（抢单）
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> takeOrder(String currentUserId, Long orderId) {
        // 当前用户必须是摄影师身份
        Photographer p = photographerMapper.getPhotographerById(currentUserId);
        if (p == null) {
            return Result.error("您的身份不是摄影师，无法接单");
        }

        // 尝试抢单
        int affectedRows = orderMapper.claimOrder(orderId, currentUserId);

        if (affectedRows > 0) {
            return Result.success(null, "恭喜，接单成功！");
        } else {
            return Result.error("手慢了，该订单已被领取或已失效");
        }
    }

    // 获取列表
    public ResponseEntity<Result> getMyOrders(String casId, String roleIdentity) {
        if ("photographer".equalsIgnoreCase(roleIdentity)) {
            return Result.success(orderMapper.selectOrdersByPhotographer(casId), "获取成功");
        } else {
            return Result.success(orderMapper.selectOrdersByCustomer(casId), "获取成功");
        }
    }

    // 订单状态流转
    @Transactional
    public ResponseEntity<Result> handleOrder(String userId, OrderDTO dto) {
        Order order = orderMapper.getOrderById(dto.getOrderId());
        if (order == null) return Result.error("订单不存在");

        String action = dto.getAction();

        // --- 安全的权限判断 ---
        // 使用 Object.equals 或先判断 null，防止空指针
        String pId = order.getPhotographerId();
        boolean isPhotographer = (pId != null && pId.equals(userId));
        boolean isCustomer = userId.equals(order.getCustomerId());

        switch (action) {
            case "ACCEPT": // 针对指定摄影师的订单
                if (pId == null || !pId.equals(userId)) return Result.error("无权操作");
                if (order.getStatus() != 0) return Result.error("订单状态已变动");
                order.setStatus(1);
                break;

            case "REJECT":
                if (pId == null || !pId.equals(userId)) return Result.error("无权操作");
                if (order.getStatus() != 0) return Result.error("订单状态已变动");
                order.setStatus(-2);
                break;

            case "CANCEL":
                if (!isCustomer) return Result.error("无权操作");
                if (order.getStatus() >= 2) return Result.error("当前状态不可取消");
                order.setStatus(-1);
                break;

            case "PAY":
                if (!isCustomer) return Result.error("无权操作");
                if (order.getStatus() != 1) return Result.error("非待支付状态");
                order.setStatus(2);
                break;

            case "DELIVER":
                // 只有当前摄影师能交付
                if (!isPhotographer) return Result.error("无权操作");
                if (order.getStatus() != 2) return Result.error("非进行中状态");
                order.setDeliverUrl(dto.getDeliverUrl());
                order.setStatus(3);
                break;

            default:
                return Result.error("未知操作指令");
        }

        orderMapper.updateOrderStatus(order);
        return Result.success(null, "操作成功: " + action);
    }

    // 评价订单
    @Transactional
    public ResponseEntity<Result> rateOrder(String userId, RatingDTO dto) {
        Order order = orderMapper.getOrderById(dto.getOrderId());

        // 只有状态3 (已完成) 且相关人员才能评价
        if (order == null || order.getStatus() != 3) {
            return Result.error("订单未完成，无法评价");
        }
        if (!userId.equals(order.getCustomerId()) && !userId.equals(order.getPhotographerId())) {
            System.out.println(order.getCustomerId() + " " + userId + " " + order.getPhotographerId());
            return Result.error("非订单相关人员");
        }

        Rating rating = new Rating();
        rating.setOrderId(order.getOrderId());
        rating.setReviewerId(userId);
        rating.setTargetId(order.getPhotographerId());
        rating.setScore(dto.getScore());
        rating.setContent(dto.getContent());
        rating.setCreatedAt(LocalDateTime.now());


        try {
            ratingMapper.insertRating(rating);
            return Result.success(null, "评价发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("数据库操作失败: " + e.getMessage());
        }
    }
}