package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.data.po.Game;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GameMapper extends BaseMapper<Game> {

    @Select("SELECT * FROM game ")
    List<Game> getAllGames();

}
