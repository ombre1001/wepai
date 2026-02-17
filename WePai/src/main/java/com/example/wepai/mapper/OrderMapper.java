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

    // 查询客户订单
    @Select("SELECT o.*, u.nickname as targetName, u.avatar_url as targetAvatar " +
            "FROM orders o " +
            "LEFT JOIN user u ON o.photographer_id = u.cas_id " +
            "WHERE o.customer_id = #{casId} AND o.status > -3 " +
            "ORDER BY o.created_at DESC")
    List<Map<String, Object>> selectOrdersByCustomer(String casId);

    // 查询摄影师订单
    @Select("SELECT o.*, u.nickname as targetName, u.avatar_url as targetAvatar " +
            "FROM orders o " +
            "LEFT JOIN user u ON o.customer_id = u.cas_id " +
            "WHERE o.photographer_id = #{casId} " +
            "ORDER BY o.created_at DESC")
    List<Map<String, Object>> selectOrdersByPhotographer(String casId);

    // 抢单/接单
    @Update("UPDATE orders SET photographer_id = #{photographerId}, status = 1 " +
            "WHERE order_id = #{orderId} AND status = 0 AND photographer_id IS NULL")
    int claimOrder(@Param("orderId") Long orderId, @Param("photographerId") String photographerId);

    // 插入订单
    @Insert("INSERT INTO orders (customer_id, photographer_id, type, shoot_time, duration, location, subject_count, price, need_equipment, contact_info, remark, status, created_at) " +
            "VALUES (#{customerId}, #{photographerId}, #{type}, #{shootTime}, #{duration}, #{location}, #{subjectCount}, #{price}, #{needEquipment}, #{contactInfo}, #{remark}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "orderId", keyColumn = "order_id")
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
            @Result(column = "deliver_url", property = "deliverUrl"),
            @Result(column = "created_at", property = "createdAt")
    })
    Order getOrderById(Long orderId);

    // 更新状态
    @Update("UPDATE orders SET status = #{status}, deliver_url = #{deliverUrl} WHERE order_id = #{orderId}")
    int updateOrderStatus(Order order);

    // 大厅列表
    @Select("SELECT * FROM orders WHERE photographer_id IS NULL AND status = 0 ORDER BY created_at DESC")
    List<Order> selectLobbyOrdersPaged(Page<Order> page);


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
    List<Map<String, Object>> selectPendingOrdersForPhotographer(@Param("photographerId") String photographerId);

}
