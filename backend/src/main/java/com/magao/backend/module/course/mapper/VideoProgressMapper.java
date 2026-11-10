package com.magao.backend.module.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.course.entity.VideoProgress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频进度Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface VideoProgressMapper extends BaseMapper<VideoProgress> {
}
