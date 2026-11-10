package com.magao.backend.module.growth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.growth.entity.PointLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface PointLogMapper extends BaseMapper<PointLog> {

    /** 统计某用户自指定时间起的积分变动合计（正数增加） */
    @Select("SELECT COALESCE(SUM(change_point),0) FROM point_log WHERE user_id=#{userId} AND create_time >= #{start}")
    Integer sumChangePointSince(@Param("userId") Long userId, @Param("start") LocalDateTime start);

    /** 统计某用户自指定时间起的有积分记录的天数（学习天数） */
    @Select("SELECT COUNT(DISTINCT DATE(create_time)) FROM point_log WHERE user_id=#{userId} AND create_time >= #{start}")
    Integer countStudyDaysSince(@Param("userId") Long userId, @Param("start") LocalDateTime start);
}
