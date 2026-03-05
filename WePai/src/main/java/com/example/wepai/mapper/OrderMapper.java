package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.dto.DraftListDTO;
import com.example.wepai.data.po.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("<script>" +
            "SELECT o.*, " +
            "CASE WHEN o.customer_id = #{userId} THEN u_p.nickname ELSE u_c.nickname END as targetName, " +
            "CASE WHEN o.customer_id = #{userId} THEN u_p.avatar_url ELSE u_c.avatar_url END as targetAvatar " +
            "FROM orders o " +
            "LEFT JOIN user u_p ON o.photographer_id = u_p.cas_id " + // 关联摄影师信息
            "LEFT JOIN user u_c ON o.customer_id = u_c.cas_id " +     // 关联顾客信息
            "WHERE (o.customer_id = #{userId} OR o.photographer_id = #{userId}) " +
            "<if test='status != null'> AND o.status = #{status} </if> " +
            "ORDER BY o.created_at DESC" +
            "</script>")
    List<Map<String, Object>> selectAllMyOrders(Page<?> page, @Param("userId") String userId, @Param("status") Integer status);


    // 抢单/接单
    @Update("UPDATE orders SET photographer_id = #{photographerId}, status = 1 " +
            "WHERE order_id = #{orderId} AND status = 0 AND photographer_id IS NULL")
    int claimOrder(@Param("orderId") Long orderId, @Param("photographerId") String photographerId);

    // 插入订单
    @Insert("""
    INSERT INTO orders (
        customer_id, photographer_id, type, shoot_time, duration, 
        location, subject_count, price, need_equipment, 
        contact_info, remark, deliver_url, status, created_at
    ) 
    VALUES (
        #{customerId}, #{photographerId}, #{type}, #{shootTime}, #{duration}, 
        #{location}, #{subjectCount}, #{price}, #{needEquipment}, 
        #{contactInfo}, #{remark}, 
        #{deliverUrl, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, 
        #{status}, #{createdAt}
    )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "orderId") // 确保返回自增ID
    int insertOrder(Order order);

    //根据ID获取
    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    @Results({
            @Result(column = "order_id", property = "orderId", id = true),
            @Result(column = "customer_id", property = "customerId"),
            @Result(column = "photographer_id", property = "photographerId"),
            @Result(column = "shoot_time", property = "shootTime"),
            @Result(column = "subject_count", property = "subjectCount"),   // 新增映射
            @Result(column = "need_equipment", property = "needEquipment"), // 新增映射
            @Result(column = "contact_info", property = "contactInfo"),     // 新增映射
            @Result(column = "deliver_url", property = "deliverUrl",
                    typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class),
            @Result(column = "created_at", property = "createdAt")
    })
    Order getOrderById(Long orderId);

    // 更新状态
    @Update("UPDATE orders SET status = #{status}, " +
            "deliver_url = #{deliverUrl, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler} " +
            "WHERE order_id = #{orderId}")
    int updateOrderStatus(Order order);

    // 大厅列表
    @Select("""
        SELECT 
            o.*, 
            u.nickname as customerName, 
            u.avatar_url as customerAvatar 
        FROM orders o 
        LEFT JOIN user u ON o.customer_id = u.cas_id 
        WHERE o.photographer_id IS NULL AND o.status = 0 
        ORDER BY o.created_at DESC
        """)
    @Results({
            @Result(column = "order_id", property = "orderId", id = true),
            @Result(column = "shoot_time", property = "shootTime"),
            @Result(column = "subject_count", property = "subjectCount"),   // 新增映射
            @Result(column = "need_equipment", property = "needEquipment"), // 新增映射
            @Result(column = "contact_info", property = "contactInfo"),     // 新增映射
            @Result(column = "deliver_url", property = "deliverUrl",
                    typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Map<String, Object>> selectLobbyOrdersPaged(Page<?> page);

    @Select("""
        SELECT 
            o.order_id, o.customer_id, o.photographer_id, o.type, o.shoot_time, 
            o.duration, o.location, o.subject_count, o.price, o.need_equipment, 
            o.contact_info, o.remark, o.status, o.created_at, 
            o.deliver_url,
            uc.nickname AS customer_name, uc.avatar_url AS customer_avatar,
            up.nickname AS photographer_name, up.avatar_url AS photographer_avatar,
            r.photo_score, r.time_score, r.comm_score, r.score, 
            r.content AS rating_content, r.created_at AS rating_time
        FROM orders o
        LEFT JOIN user uc ON o.customer_id = uc.cas_id
        LEFT JOIN user up ON o.photographer_id = up.cas_id
        LEFT JOIN ratings r ON o.order_id = r.order_id
        WHERE o.order_id = #{orderId}
        """)
    Map<String, Object> selectOrderDetailFull(@Param("orderId") Long orderId);


    @Select("SELECT order_id AS orderId, location, created_at AS createdAt " +
            "FROM orders " +
            "WHERE customer_id = #{customerId} AND status = -3 " +
            "ORDER BY created_at DESC")
    List<DraftListDTO> selectDraftListPaged(Page<DraftListDTO> page, @Param("customerId") String customerId);

    @Delete("DELETE FROM orders WHERE order_id = #{orderId} " +
            "AND customer_id = #{customerId} AND status = -3")
    int deleteDraftManual(@Param("orderId") Long orderId, @Param("customerId") String customerId);

    @Select("SELECT o.*, u.nickname as customerName, u.avatar_url as customerAvatar " +
            "FROM orders o " +
            "LEFT JOIN user u ON o.customer_id = u.cas_id " +
            "WHERE o.photographer_id = #{photographerId} " +
            "AND o.status = 0 " +
            "ORDER BY o.created_at DESC")
    List<Map<String, Object>> selectPendingOrdersForPhotographer(Page<?> page, @Param("photographerId") String photographerId);

    /**
     * 根据订单ID查询评价详情
     */
    @Select("""
        SELECT 
            r.created_at AS createdAt, 
            u.avatar_url AS avatar, 
            u.nickname, 
            r.order_id AS orderId, 
            r.photo_score AS photoScore, 
            r.time_score AS timeScore, 
            r.comm_score AS commScore, 
            r.content 
        FROM ratings r
        LEFT JOIN user u ON r.reviewer_id = u.cas_id
        WHERE r.order_id = #{orderId}
        """)
    Map<String, Object> selectReviewByOrderId(@Param("orderId") Long orderId);

    /**
     * 分页查询摄影师已接取的订单 (status = 2)
     */
    @Select("""
        SELECT * FROM orders 
        WHERE photographer_id = #{photographerId} 
        AND status = 1 
        ORDER BY shoot_time DESC
        """)
    List<Map<String, Object>> selectAcceptedOrdersForPhotographer(
            Page<?> page,
            @Param("photographerId") String photographerId);

    @Select("<script>" +
            "SELECT o.order_id, o.deliver_url,o.photographer_id, o.shoot_time, o.location, o.type, " +
            "u.nickname as photographerName, u.avatar_url as photographerAvatar " +
            "FROM orders o " +
            "LEFT JOIN user u ON o.photographer_id = u.cas_id " +
            "WHERE o.status IN (3, 4) " +
            "AND o.deliver_url IS NOT NULL AND o.deliver_url != '' " +
            "<if test='photographerId != null and photographerId != \"\"'>" +
            "  AND o.photographer_id = #{photographerId} " +
            "</if>" +
            "ORDER BY o.shoot_time DESC" +
            "</script>")
    @Results({
            // 必须保留 TypeHandler 处理，否则获取到的是 JSON 字符串
            @Result(column = "deliver_url", property = "deliverUrl",
                    typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    })
    List<Map<String, Object>> selectWorksCombined(Page<?> page, @Param("photographerId") String photographerId);

    @Select("SELECT o.*, u.nickname as customerName, u.avatar_url as customerAvatar " +
            "FROM orders o " +
            "LEFT JOIN user u ON o.customer_id = u.cas_id " +
            "WHERE o.photographer_id = #{photographerId} " +
            "AND o.status IN (3, 4) " + // 必须是已完成
            "AND o.deliver_url IS NOT NULL " + // 且必须有作品图
            "ORDER BY o.shoot_time DESC")
    List<Map<String, Object>> selectPhotographerWorks(Page<?> page, @Param("photographerId") String photographerId);

    @Select("SELECT COUNT(*) FROM orders WHERE customer_id = #{userId} OR photographer_id = #{userId}")
    int countTotalOrders(@Param("userId") String userId);

    // 统计已完成订单量（状态为 3 的订单）
    @Select("SELECT COUNT(*) FROM orders WHERE (customer_id = #{userId} OR photographer_id = #{userId}) AND status IN (3, 4)")
    int countCompletedOrders(@Param("userId") String userId);

    @Update("""
        UPDATE orders SET 
            photographer_id = #{photographerId},
            type = #{type},
            shoot_time = #{shootTime},
            duration = #{duration},
            location = #{location},
            subject_count = #{subjectCount},
            price = #{price},
            need_equipment = #{needEquipment},
            contact_info = #{contactInfo},
            remark = #{remark},
            status = #{status}
        WHERE order_id = #{orderId} AND customer_id = #{customerId}
        """)
    int updateOrderManual(Order order);

    // 管理员查看所有订单
    @Select("SELECT o.*, uc.nickname as customerName, up.nickname as photographerName " +
            "FROM orders o " +
            "LEFT JOIN user uc ON o.customer_id = uc.cas_id " +
            "LEFT JOIN user up ON o.photographer_id = up.cas_id " +
            "ORDER BY o.created_at DESC")
    List<Map<String, Object>> selectAllOrdersAdmin(Page<?> page);


}

