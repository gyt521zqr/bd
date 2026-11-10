package com.magao.backend.module.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.exam.entity.ExamPaper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface ExamPaperMapper extends BaseMapper<ExamPaper> {
}
