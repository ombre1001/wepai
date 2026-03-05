package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.wepai.data.po.Rating;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;


@Mapper
public interface RatingMapper extends BaseMapper<Rating> {
    @Insert("""
        INSERT INTO ratings (
            order_id, reviewer_id, target_id, 
            photo_score, time_score, comm_score, score, content, created_at
        ) VALUES (
            #{orderId}, #{reviewerId}, #{targetId}, 
            #{photoScore}, #{timeScore}, #{commScore}, #{score}, #{content}, NOW()
        )
        """)
    int insertRating(Rating rating);
}