package com.magao.backend.module.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.module.exam.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 考试题目Mapper接口（题库单源，P1b 新增）
 *
 * @author Magao开发团队
 * @since 2026-01-01
 */
@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

    /**
     * 按试卷ID查询题目（按关联表 sort_order 排序）。
     * 不走逻辑删除自动注入，需手动过滤 deleted=0。
     */
    @Select("SELECT q.* FROM exam_question q " +
            "INNER JOIN exam_paper_question pq ON q.id = pq.question_id " +
            "WHERE pq.paper_id = #{paperId} AND q.deleted = 0 " +
            "ORDER BY pq.sort_order ASC")
    List<ExamQuestion> selectByPaperId(@Param("paperId") Long paperId);
}
