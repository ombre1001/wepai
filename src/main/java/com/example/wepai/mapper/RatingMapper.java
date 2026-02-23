package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.wepai.data.po.Rating;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;


@Mapper
public interface RatingMapper extends BaseMapper<Rating> {
    @Insert("INSERT INTO ratings (order_id, reviewer_id, target_id, score, content, created_at) " +
            "VALUES (#{orderId}, #{reviewerId}, #{targetId}, #{score}, #{content}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "ratingId")
    int insertRating(Rating rating);
}