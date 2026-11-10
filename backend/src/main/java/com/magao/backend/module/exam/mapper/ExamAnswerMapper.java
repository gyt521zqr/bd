package com.magao.backend.module.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.exam.entity.ExamAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 考试答案Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface ExamAnswerMapper extends BaseMapper<ExamAnswer> {

    /**
     * 根据记录ID查询所有答案
     */
    @Select("SELECT * FROM exam_answer WHERE record_id = #{recordId} ORDER BY id ASC")
    List<ExamAnswer> selectByRecordId(@Param("recordId") Long recordId);
}
