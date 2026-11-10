package com.magao.backend.module.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.exam.entity.ExamGradingTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试批改任务Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface ExamGradingTaskMapper extends BaseMapper<ExamGradingTask> {
}
