package com.magao.backend.module.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.exam.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 考试记录Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    /**
     * 查询用户的考试记录列表
     */
    @Select("SELECT * FROM exam_record WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<ExamRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户某次考试的最新记录
     */
    @Select("SELECT * FROM exam_record WHERE user_id = #{userId} AND exam_id = #{examId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    ExamRecord selectLatestByUserAndExam(@Param("userId") Long userId, @Param("examId") Long examId);
}
