package com.magao.backend.module.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.exam.entity.ExamInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface ExamMapper extends BaseMapper<ExamInfo> {
}
