package com.example.wepai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.dto.DraftListDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RatingMapper ratingMapper;
    @Resource
    private PhotographerMapper photographerMapper;

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> saveDraft(String customerId, OrderDTO dto) {
        Order order;
        if (dto.getOrderId() != null) {
            order = orderMapper.selectById(dto.getOrderId());
            if (order == null || !order.getCustomerId().equals(customerId)) {
                return Result.error("草稿不存在或无权操作");
            }
        } else {
            order = new Order();
            order.setCustomerId(customerId);
            order.setCreatedAt(LocalDateTime.now());
        }

        // 填充 DTO 数据
        copyDtoToOrder(dto, order);

        order.setStatus(-3); // 设为草稿状态

        order.setCreatedAt(LocalDateTime.now());

        if (order.getOrderId() != null) {
            orderMapper.updateById(order);
        } else {
            orderMapper.insertOrder(order); // 使用你自定义的插入方法
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("orderId", order.getOrderId());
        resMap.put("savedAt", order.getCreatedAt()); // 返回保存时间

        return Result.success(resMap, "草稿保存成功");
    }

    public ResponseEntity<Result> getDraftList(String customerId, int pageNum, int pageSize) {
        Page<DraftListDTO> page = new Page<>(pageNum, pageSize);

        List<DraftListDTO> list = orderMapper.selectDraftListPaged(page, customerId);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());

        return Result.success(data, "获取草稿列表成功");
    }

    public ResponseEntity<Result> getDraftDetail(String customerId, Long orderId) {
        Order order = orderMapper.getOrderById(orderId);

        if (order == null || !order.getStatus().equals(-3) || !order.getCustomerId().equals(customerId)) {
            return Result.error("草稿不存在或无权查看");
        }

        return Result.success(order, "获取草稿详情成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> deleteDraft(String customerId, Long orderId) {
        // 查询订单是否存在
        Order order = orderMapper.getOrderById(orderId);

        if (order == null) {
            return Result.error("该草稿不存在");
        }

        // 只能删除自己的草稿
        if (!order.getCustomerId().equals(customerId)) {
            return Result.error("无权删除他人的草稿");
        }

        // 只能删除状态为 -3 的订单
        if (order.getStatus() != -3) {
            return Result.error("该订单已发布或处于其他状态，无法通过此接口删除");
        }

        // 执行删除
        int rows = orderMapper.deleteDraftManual(orderId, customerId);

        if (rows > 0) {
            return Result.success(null, "草稿删除成功");
        } else {
            return Result.error("删除失败，请稍后重试");
        }
    }

    // 创建订单
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> createOrder(String customerId, OrderDTO dto) {
        Order order;
        // 如果是把现有的草稿转为正式订单
        if (dto.getOrderId() != null) {
            order = orderMapper.selectById(dto.getOrderId());
            if (order == null) return Result.error("订单不存在");
        } else {
            order = new Order();
            order.setCustomerId(customerId);
            order.setCreatedAt(LocalDateTime.now());
        }

        copyDtoToOrder(dto, order);

        // 发布逻辑：判断是直接给摄影师还是去大厅
        if (dto.getPhotographerId() != null && !dto.getPhotographerId().isBlank()) {
            order.setPhotographerId(dto.getPhotographerId());
            order.setStatus(0);
        } else {
            order.setPhotographerId(null);
            order.setStatus(0); // 待接单
        }

        if (order.getOrderId() != null) {
            orderMapper.updateById(order);
        } else {
            orderMapper.insertOrder(order);
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("orderId", order.getOrderId());
        resMap.put("createTime", order.getCreatedAt());

        return Result.success(resMap, "订单创建成功");
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
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("ratingId", rating.getRatingId());
            resMap.put("createTime", rating.getCreatedAt());

            return Result.success(resMap, "评价发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("数据库操作失败: " + e.getMessage());
        }
    }

    public ResponseEntity<Result> getLobbyOrders(int pageNum, int pageSize) {
        // 准备分页参数
        Page<Order> page = new Page<>(pageNum, pageSize);

        // 查询
        List<Order> list = orderMapper.selectLobbyOrdersPaged(page);

        // 封装返回
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());

        return Result.success(data, "获取大厅订单成功");
    }

    private void copyDtoToOrder(OrderDTO dto, Order order) {
        order.setType(dto.getType());
        order.setShootTime(dto.getShootTime());
        order.setDuration(dto.getDuration());
        order.setLocation(dto.getLocation());
        order.setSubjectCount(dto.getSubjectCount());
        order.setPrice(dto.getPrice());
        order.setNeedEquipment(dto.getNeedEquipment());
        order.setContactInfo(dto.getContactInfo());
        order.setRemark(dto.getRemark());
    }

    public ResponseEntity<Result> getPendingOrders(String photographerId) {
        List<Map<String, Object>> list = orderMapper.selectPendingOrdersForPhotographer(photographerId);
        return Result.success(list, "获取待处理订单成功");
    }
}