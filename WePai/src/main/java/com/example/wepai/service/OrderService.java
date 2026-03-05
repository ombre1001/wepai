package com.example.wepai.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.dto.DraftListDTO;
import com.example.wepai.data.dto.OrderDTO;
import com.example.wepai.data.dto.RatingDTO;
import com.example.wepai.data.po.Order;
import com.example.wepai.data.po.Photographer;
import com.example.wepai.data.po.Rating;
import com.example.wepai.data.po.User;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.OrderMapper;
import com.example.wepai.mapper.PhotographerMapper;
import com.example.wepai.mapper.RatingMapper;
import com.example.wepai.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Resource
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> saveDraft(String customerId, OrderDTO dto) {
        Order order;
        if (dto.getOrderId() != null) {
            order = orderMapper.selectById(dto.getOrderId());
            if (order == null || !order.getCustomerId().equals(customerId)) {
                return Result.error("草稿不存在或无权操作");
            }
        } else {
            Page<DraftListDTO> countPage = new Page<>(1, 10); // 只需要看有没有 5 个
            List<DraftListDTO> existingDrafts = orderMapper.selectDraftListPaged(countPage, customerId);
            if (existingDrafts != null && existingDrafts.size() >= 5) {
                return Result.error("草稿箱已满（最多存储5个），请删除部分草稿后再试");
            }
            order = new Order();
            order.setCustomerId(customerId);
            order.setCreatedAt(LocalDateTime.now());
        }

        // 填充 DTO 数据
        copyDtoToOrder(dto, order);

        order.setStatus(-3); // 设为草稿状态

        order.setCreatedAt(LocalDateTime.now());

        if (order.getOrderId() != null) {
            orderMapper.updateOrderManual(order);
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
            order = orderMapper.getOrderById(dto.getOrderId());
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
            orderMapper.updateOrderManual(order);
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

    public ResponseEntity<Result> getMyOrders(String userId, Integer status, int pageNum, int pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);

        // 1. 查询所有相关订单
        List<Map<String, Object>> list = orderMapper.selectAllMyOrders(page, userId, status);

        // 2. 遍历列表，添加身份标识
        list.forEach(item -> {
            // 使用 String.valueOf() 安全地将 Long 转换为 String
            // 或者先获取 Object 再进行判断
            Object dbCustomerId = item.get("customer_id");
            String customerIdStr = dbCustomerId == null ? "" : String.valueOf(dbCustomerId);

            // 同样，确保 userId 也是字符串进行比较
            if (String.valueOf(userId).equals(customerIdStr)) {
                item.put("isMyOrderAsCustomer", true);
            } else {
                item.put("isMyOrderAsCustomer", false);
            }
        });

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());

        return Result.success(data, "获取订单列表成功");
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
                if (!isPhotographer) return Result.error("无权操作");
                if (order.getStatus() != 2) return Result.error("非进行中状态");

                // 因为 dto 和 order 里的 deliverUrl 都变成了 List<String>，直接 set 过去即可
                // MyBatis 存入数据库时会自动变成 '["url1", "url2"]'
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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Result> rateOrder(String userId, RatingDTO dto) {
        // 1. 校验订单是否存在及归属（使用你之前定义的查询方法）
        Order order = orderMapper.getOrderById(dto.getOrderId());
        if (order == null || !order.getCustomerId().equals(userId)) {
            return Result.error("订单不存在或无权评价");
        }

        // （可选）校验订单状态是否允许评价，例如 status == 3 才允许评价
        if (order.getStatus() != 3) {
            return Result.error("当前订单状态不允许评价");
        }

        // 2. 核心逻辑：计算平均分 (保留一位小数)
        // 注意要转成 double 进行计算，否则整数除法会丢失精度
        double rawAvg = (dto.getPhotoScore() + dto.getTimeScore() + dto.getCommScore()) / 3.0;
        double finalScore = Math.round(rawAvg * 10.0) / 10.0; // 例如 13/3 = 4.333 -> 4.3

        // 3. 构建评价实体
        Rating rating = new Rating();
        rating.setOrderId(dto.getOrderId());
        rating.setReviewerId(userId);
        rating.setTargetId(order.getPhotographerId());

        // 塞入三个维度的分数和计算出的总分
        rating.setPhotoScore(dto.getPhotoScore());
        rating.setTimeScore(dto.getTimeScore());
        rating.setCommScore(dto.getCommScore());
        rating.setScore(finalScore);

        rating.setContent(dto.getContent());

        // 4. 插入评价数据
        ratingMapper.insertRating(rating);

        // 5. 更新订单状态为已评价（假设 4 代表已完成/已评价）
        order.setStatus(4);
        orderMapper.updateOrderManual(order); // 使用你之前手写的更新方法
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("ratingId", rating.getRatingId());
        resMap.put("createTime", rating.getCreatedAt());

        return Result.success(resMap, "评价成功！");

    }

    public ResponseEntity<Result> getLobbyOrders(int pageNum, int pageSize) {
        // 准备分页参数
        Page<Order> page = new Page<>(pageNum, pageSize);

        // 查询
        List<Map<String, Object>> list = orderMapper.selectLobbyOrdersPaged(page);

        // 封装返回
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());

        return Result.success(data, "获取大厅订单成功");
    }

    public ResponseEntity<Result> getOrderDetail( Long orderId) {
        Map<String, Object> detail = orderMapper.selectOrderDetailFull(orderId);

        if (detail == null) {
            return Result.error("订单不存在");
        }


        Object deliverUrlObj = detail.get("deliver_url");
        if (deliverUrlObj != null) {
            String deliverUrlStr = String.valueOf(deliverUrlObj);
            if (deliverUrlStr.startsWith("[")) {
                try {

                    List<String> urls = JSONUtil.toList(deliverUrlStr, String.class);
                    detail.put("deliver_url", urls);
                } catch (Exception e) {
                    detail.put("deliver_url", new ArrayList<>());
                }
            } else if (!deliverUrlStr.isBlank()) {
                detail.put("deliver_url", List.of(deliverUrlStr));
            } else {
                detail.put("deliver_url", new ArrayList<>());
            }
        } else {
            detail.put("deliver_url", new ArrayList<>());
        }

        return Result.success(detail, "获取订单详情成功");
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

    public ResponseEntity<Result> getPendingOrders(String photographerId, int pageNum, int pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);

        List<Map<String, Object>> list = orderMapper.selectPendingOrdersForPhotographer(page, photographerId);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());  // 总记录数
        data.put("pages", page.getPages());  // 总页数

        return Result.success(data, "获取待处理订单成功");
    }
    /**
     * 通过订单ID获取评价
     */
    public ResponseEntity<Result> getReviewByOrderId(Long orderId) {
        Map<String, Object> review = orderMapper.selectReviewByOrderId(orderId);

        if (review == null) {
            return Result.error("该订单暂无评价");
        }

        return Result.success(review, "获取订单评价成功");
    }

    public ResponseEntity<Result> getAcceptedOrders(String photographerId, int pageNum, int pageSize) {
        // 1. 初始化分页对象
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);

        // 2. 执行查询
        List<Map<String, Object>> list = orderMapper.selectAcceptedOrdersForPhotographer(page, photographerId);

        // 3. 封装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());

        return Result.success(data, "获取已接订单成功");
    }

    /**
     * 公开的作品展示列表
     */


    public ResponseEntity<Result> getWorks(String photographerId, int pageNum, int pageSize) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        List<Map<String, Object>> list = orderMapper.selectWorksCombined(page, photographerId);

        list.forEach(item -> {
            // 1. 统一处理交付图：只取第一张作为封面展示
            Object rawData = item.get("deliverUrl"); // 经过 TypeHandler 已经是 List 或 String
            String firstUrl = "";

            if (rawData instanceof List) {
                List<?> urls = (List<?>) rawData;
                if (!urls.isEmpty()) firstUrl = String.valueOf(urls.get(0));
            } else if (rawData instanceof String) {
                String str = (String) rawData;
                if (str.startsWith("[")) {
                    List<String> urls = JSONUtil.toList(str, String.class);
                    if (!urls.isEmpty()) firstUrl = urls.get(0);
                } else {
                    firstUrl = str;
                }
            }

            // 2. 规范化返回字段，移除多余字段
            item.put("cover_url", firstUrl);
            item.remove("deliverUrl");
            item.remove("deliver_url");
            item.remove("customer_id"); // 安全起见移除顾客ID
        });

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", page.getTotal());
        data.put("pages", page.getPages());

        String msg = (photographerId == null) ? "获取作品广场成功" : "获取摄影师作品集成功";
        return Result.success(data, msg);
    }

}