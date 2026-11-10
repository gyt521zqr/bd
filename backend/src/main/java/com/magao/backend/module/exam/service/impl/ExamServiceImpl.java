package com.magao.backend.module.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.exception.ExamException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.exam.entity.ExamAnswer;
import com.magao.backend.module.exam.entity.ExamGradingTask;
import com.magao.backend.module.exam.entity.ExamInfo;
import com.magao.backend.module.exam.entity.ExamPaper;
import com.magao.backend.module.exam.entity.ExamRecord;
import com.magao.backend.module.exam.mapper.ExamAnswerMapper;
import com.magao.backend.module.exam.mapper.ExamGradingTaskMapper;
import com.magao.backend.module.exam.mapper.ExamMapper;
import com.magao.backend.module.exam.mapper.ExamPaperMapper;
import com.magao.backend.module.exam.mapper.ExamQuestionMapper;
import com.magao.backend.module.exam.mapper.ExamRecordMapper;
import com.magao.backend.module.exam.entity.ExamQuestion;
import com.magao.backend.module.exam.service.ExamService;
import com.magao.backend.module.exam.vo.ExamQuestionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl extends ServiceImpl<ExamMapper, ExamInfo> implements ExamService {

    private final ExamRecordMapper examRecordMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamGradingTaskMapper examGradingTaskMapper;
    private final ExamQuestionMapper examQuestionMapper;

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public Page<ExamInfo> pageList(PageDTO pageDTO, Integer examType, Integer status) {
        Page<ExamInfo> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<ExamInfo> wrapper = new LambdaQueryWrapper<>();
        if (examType != null) { wrapper.eq(ExamInfo::getExamType, examType); }
        if (status != null) { wrapper.eq(ExamInfo::getStatus, status); }
        wrapper.orderByDesc(ExamInfo::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public ExamInfo getDetail(Long examId) {
        ExamInfo exam = getById(examId);
        if (exam == null) { throw new BusinessException(ErrorCode.EXAM_NOT_FOUND); }
        return exam;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamRecord enterExam(Long userId, Long examId) {
        ExamInfo exam = getById(examId);
        if (exam == null) { throw new ExamException(ErrorCode.EXAM_NOT_FOUND); }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) { throw new ExamException(ErrorCode.EXAM_NOT_STARTED); }
        if (now.isAfter(exam.getEndTime())) { throw new ExamException(ErrorCode.EXAM_FINISHED); }

        // 检查是否已有进行中的记录
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId)
               .eq(ExamRecord::getExamId, examId)
               .eq(ExamRecord::getStatus, 1)
               .orderByDesc(ExamRecord::getCreateTime)
               .last("LIMIT 1");
        ExamRecord existing = examRecordMapper.selectOne(wrapper);
        if (existing != null) {
            log.info("用户已有进行中的考试记录，直接返回：recordId={}", existing.getId());
            return existing;
        }

        // 创建新记录
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setUserId(userId);
        // 查询考试关联的试卷，记录试卷ID（ExamInfo 不直接持有 paperId）
        LambdaQueryWrapper<ExamPaper> paperWrapper = new LambdaQueryWrapper<>();
        paperWrapper.eq(ExamPaper::getExamId, examId)
                .orderByDesc(ExamPaper::getCreateTime)
                .last("LIMIT 1");
        ExamPaper paper = examPaperMapper.selectOne(paperWrapper);
        record.setPaperId(paper != null ? paper.getId() : null);
        record.setStartTime(now);
        record.setStatus(1);
        record.setSwitchScreenCount(0);
        record.setLeavePageCount(0);
        record.setViolationFlag(0);
        record.setScore(0);
        record.setIsPassed(0);
        examRecordMapper.insert(record);
        log.info("进入考试：userId={}, examId={}, recordId={}", userId, examId, record.getId());
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamRecord submitExam(Long userId, Long examId, Long recordId) {
        ExamInfo exam = getById(examId);
        if (exam == null) { throw new ExamException(ErrorCode.EXAM_NOT_FOUND); }

        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new ExamException(ErrorCode.EXAM_RECORD_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        record.setSubmitTime(now);
        record.setStatus(2);

        // 计算得分
        List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(recordId);
        int totalScore = 0;
        for (ExamAnswer answer : answers) {
            if (answer.getScore() != null) {
                totalScore += answer.getScore();
            }
        }
        record.setScore(totalScore);
        record.setIsPassed(totalScore >= exam.getPassScore() ? 1 : 0);

        examRecordMapper.updateById(record);
        log.info("提交考试：userId={}, examId={}, recordId={}, score={}", userId, examId, recordId, totalScore);
        return record;
    }

    @Override
    public ExamRecord getResult(Long userId, Long recordId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new ExamException(ErrorCode.EXAM_RECORD_NOT_FOUND);
        }
        log.info("查询考试结果：userId={}, recordId={}", userId, recordId);
        return record;
    }

    /**
     * 保存考试答案
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAnswer(Long recordId, Long questionId, String userAnswer, String code, Integer language) {
        LambdaQueryWrapper<ExamAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamAnswer::getRecordId, recordId)
               .eq(ExamAnswer::getQuestionId, questionId);
        ExamAnswer existing = examAnswerMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setUserAnswer(userAnswer);
            existing.setCode(code);
            existing.setLanguage(language);
            existing.setUpdateTime(LocalDateTime.now());
            examAnswerMapper.updateById(existing);
        } else {
            ExamAnswer answer = new ExamAnswer();
            answer.setRecordId(recordId);
            answer.setQuestionId(questionId);
            answer.setUserAnswer(userAnswer);
            answer.setCode(code);
            answer.setLanguage(language);
            answer.setJudgeStatus(0);
            answer.setScore(0);
            answer.setIsCorrect(0);
            answer.setGradeStatus(0);
            examAnswerMapper.insert(answer);
        }
    }

    /**
     * 获取考试答案列表
     */
    public List<ExamAnswer> getAnswers(Long recordId) {
        return examAnswerMapper.selectByRecordId(recordId);
    }

    @Override
    public Page<ExamRecord> listRecords(PageDTO pageDTO, Long userId) {
        Page<ExamRecord> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId).orderByDesc(ExamRecord::getCreateTime);
        return examRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public ExamRecord queryResult(String registerNo, String name) {
        if (registerNo == null || registerNo.isBlank()) {
            return null;
        }
        Long recordId;
        try {
            recordId = Long.parseLong(registerNo.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        return examRecordMapper.selectById(recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportSwitchScreen(Long userId, Long examId) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId)
               .eq(ExamRecord::getExamId, examId)
               .in(ExamRecord::getStatus, 1, 2)
               .orderByDesc(ExamRecord::getCreateTime)
               .last("LIMIT 1");
        ExamRecord record = examRecordMapper.selectOne(wrapper);
        if (record != null) {
            int current = record.getSwitchScreenCount() == null ? 0 : record.getSwitchScreenCount();
            record.setSwitchScreenCount(current + 1);
            examRecordMapper.updateById(record);
            log.info("上报切屏：userId={}, examId={}, recordId={}, count={}", userId, examId, record.getId(), current + 1);
        }
    }

    @Override
    public Page<ExamGradingTask> listGradingTasks(PageDTO pageDTO, Long teacherId) {
        Page<ExamGradingTask> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<ExamGradingTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamGradingTask::getTeacherId, teacherId).orderByDesc(ExamGradingTask::getCreateTime);
        return examGradingTaskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeQuestion(Long recordId, Long questionId, Integer score) {
        LambdaQueryWrapper<ExamAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamAnswer::getRecordId, recordId).eq(ExamAnswer::getQuestionId, questionId);
        ExamAnswer answer = examAnswerMapper.selectOne(wrapper);
        if (answer == null) {
            answer = new ExamAnswer();
            answer.setRecordId(recordId);
            answer.setQuestionId(questionId);
            answer.setGradeStatus(1);
            answer.setScore(score);
            answer.setIsCorrect(score != null && score > 0 ? 1 : 0);
            examAnswerMapper.insert(answer);
        } else {
            answer.setScore(score);
            answer.setIsCorrect(score != null && score > 0 ? 1 : 0);
            answer.setGradeStatus(1);
            examAnswerMapper.updateById(answer);
        }
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record != null) {
            List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(recordId);
            int total = 0;
            for (ExamAnswer a : answers) {
                if (a.getScore() != null) {
                    total += a.getScore();
                }
            }
            record.setScore(total);
            ExamInfo exam = getById(record.getExamId());
            if (exam != null) {
                record.setIsPassed(total >= exam.getPassScore() ? 1 : 0);
            }
            examRecordMapper.updateById(record);
            log.info("批改题目：recordId={}, questionId={}, score={}, total={}", recordId, questionId, score, total);
        }
    }

    @Override
    public List<ExamQuestionVO> listPaperQuestions(Long paperId) {
        List<ExamQuestion> questions = examQuestionMapper.selectByPaperId(paperId);
        List<ExamQuestionVO> result = new java.util.ArrayList<>(questions.size());
        for (ExamQuestion q : questions) {
            ExamQuestionVO vo = new ExamQuestionVO();
            vo.setId(q.getId());
            vo.setType(q.getType());
            vo.setTitle(q.getTitle());
            vo.setAnswer(q.getAnswer());
            vo.setAnalysis(q.getAnalysis());
            vo.setScore(q.getScore());
            vo.setLanguage(q.getLanguage());
            vo.setTemplateCode(q.getTemplateCode());
            if (q.getOptions() != null && !q.getOptions().isBlank()) {
                try {
                    vo.setOptions(JSON.readValue(q.getOptions(),
                            JSON.getTypeFactory().constructCollectionType(List.class, String.class)));
                } catch (Exception e) {
                    log.warn("题目选项解析失败：questionId={}, error={}", q.getId(), e.getMessage());
                    vo.setOptions(null);
                }
            }
            result.add(vo);
        }
        return result;
    }
}
