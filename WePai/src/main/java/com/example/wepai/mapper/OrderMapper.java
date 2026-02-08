package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wepai.data.po.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    // 查询客户的订单（关联获取摄影师信息）
    @Select("SELECT o.*, u.nickname as targetName, u.avatar_url as targetAvatar " +
            "FROM orders o " +
            "LEFT JOIN user u ON o.photographer_id = u.cas_id " +
            "WHERE o.customer_id = #{casId} " +
            "ORDER BY o.created_at DESC")
    List<Map<String, Object>> selectOrdersByCustomer(String casId);

    // 查询摄影师的订单（关联获取客户信息）
    @Select("SELECT o.*, u.nickname as targetName, u.avatar_url as targetAvatar " +
            "FROM orders o " +
            "LEFT JOIN user u ON o.customer_id = u.cas_id " +
            "WHERE o.photographer_id = #{casId} " +
            "ORDER BY o.created_at DESC")
    List<Map<String, Object>> selectOrdersByPhotographer(String casId);

    @Update("UPDATE orders SET photographer_id = #{photographerId}, status = 1 " +
            "WHERE order_id = #{orderId} AND status = 0 AND photographer_id IS NULL")
    int claimOrder(@Param("orderId") Long orderId, @Param("photographerId") String photographerId);

    @Insert("INSERT INTO orders (customer_id, photographer_id, type, shoot_time, location, price, remark, status, created_at) " +
            "VALUES (#{customerId}, #{photographerId}, #{type}, #{shootTime}, #{location}, #{price}, #{remark}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "orderId", keyColumn = "order_id")
    int insertOrder(Order order);;

    @Select("SELECT * FROM orders WHERE order_id = #{orderId}")
    @Results({
            @Result(column = "order_id", property = "orderId", id = true),
            @Result(column = "customer_id", property = "customerId"), // 重点：手动映射
            @Result(column = "photographer_id", property = "photographerId"),
            @Result(column = "shoot_time", property = "shootTime"),
            @Result(column = "deliver_url", property = "deliverUrl"),
            @Result(column = "created_at", property = "createdAt")
    })
    Order getOrderById(Long orderId);

    @Update("UPDATE orders SET status = #{status}, deliver_url = #{deliverUrl} WHERE order_id = #{orderId}")
    int updateOrderStatus(Order order);

    @Select("SELECT * FROM orders WHERE photographer_id IS NULL AND status = 0 ORDER BY created_at DESC")
    @Results({
            @Result(column = "order_id", property = "orderId", id = true),
            @Result(column = "customer_id", property = "customerId"),
            @Result(column = "photographer_id", property = "photographerId"),
            @Result(column = "shoot_time", property = "shootTime"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Order> getLobbyOrders();

}
