package com.magao.backend.module.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.practice.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 测试用例Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {

    /**
     * 根据题目ID查询其全部测试用例（按ID升序，保证判分顺序稳定）
     *
     * @param questionId 题目ID
     * @return 测试用例列表
     */
    @Select("SELECT * FROM test_case WHERE question_id = #{questionId} ORDER BY id ASC")
    List<TestCase> selectByQuestionId(@Param("questionId") Long questionId);
}
