package com.magao.backend.module.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.practice.entity.PracticeQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 练习题Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface PracticeQuestionMapper extends BaseMapper<PracticeQuestion> {
}
