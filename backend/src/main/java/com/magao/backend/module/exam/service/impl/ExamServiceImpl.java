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
import com.magao.backend.module.exam.vo.ExamRecordVO;
import com.magao.backend.module.exam.vo.ExamResultDetailVO;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final UserMapper userMapper;

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 客观题题型：交卷时自动判分 */
    private static final String TYPE_CHOICE = "choice";
    private static final String TYPE_JUDGE = "judge";
    private static final String TYPE_FILL = "fill";

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

        // P0-06：交卷时自动批改客观题（choice/judge/fill），主观题保持待人工/判题批改
        List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(recordId);
        autoGradeObjectiveQuestions(answers);

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

    /**
     * P0-06：自动批改客观题。
     * 选择题/判断题按标准答案精确比对；填空题去空白后比对（大小写不敏感）；
     * 代码/Scratch 题不自动给分，保持 gradeStatus=0 等待判题或教师批改。
     */
    private void autoGradeObjectiveQuestions(List<ExamAnswer> answers) {
        if (answers == null || answers.isEmpty()) return;
        List<Long> questionIds = answers.stream()
                .map(ExamAnswer::getQuestionId).filter(java.util.Objects::nonNull).distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) return;
        Map<Long, ExamQuestion> questionMap = examQuestionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(ExamQuestion::getId, Function.identity(), (a, b) -> a));

        for (ExamAnswer answer : answers) {
            ExamQuestion q = questionMap.get(answer.getQuestionId());
            if (q == null || q.getAnswer() == null || q.getAnswer().isBlank()) continue;
            String type = q.getType() == null ? "" : q.getType().toLowerCase(Locale.ROOT);
            boolean objective = TYPE_CHOICE.equals(type) || TYPE_JUDGE.equals(type) || TYPE_FILL.equals(type);
            if (!objective) continue; // 主观题留给判题/教师

            boolean correct = normalizeAnswer(answer.getUserAnswer()).equals(normalizeAnswer(q.getAnswer()));
            int score = correct && q.getScore() != null ? q.getScore() : 0;
            answer.setScore(score);
            answer.setIsCorrect(correct ? 1 : 0);
            answer.setGradeStatus(1); // 已批改
            answer.setJudgeStatus(1);
            answer.setUpdateTime(LocalDateTime.now());
            examAnswerMapper.updateById(answer);
        }
    }

    /** 答案规范化：去首尾空白、忽略大小写（选择题 A/a、判断题 true/TRUE） */
    private String normalizeAnswer(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoGradeRecord(Long recordId) {
        List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(recordId);
        // 仅重判尚未批改（gradeStatus=0/null）的客观题，幂等
        List<ExamAnswer> pending = answers.stream()
                .filter(a -> a.getGradeStatus() == null || a.getGradeStatus() == 0)
                .collect(Collectors.toList());
        autoGradeObjectiveQuestions(pending);
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record != null) {
            int total = answers.stream()
                    .mapToInt(a -> a.getScore() == null ? 0 : a.getScore()).sum();
            record.setScore(total);
            ExamInfo exam = getById(record.getExamId());
            if (exam != null && exam.getPassScore() != null) {
                record.setIsPassed(total >= exam.getPassScore() ? 1 : 0);
            }
            examRecordMapper.updateById(record);
        }
        log.info("自动判分完成：recordId={}", recordId);
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
    public void saveAnswer(Long recordId, Long questionId, String userAnswer, String code, Integer language, Long currentUserId) {
        // P0-14：校验答卷归属，只能写自己的考试记录，且必须处于进行中
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || currentUserId == null || !currentUserId.equals(record.getUserId())
                || !Integer.valueOf(1).equals(record.getStatus())) {
            throw new ExamException(ErrorCode.EXAM_RECORD_NOT_FOUND);
        }
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
    public Page<ExamRecordVO> listRecords(PageDTO pageDTO, Long userId) {
        Page<ExamRecord> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId).orderByDesc(ExamRecord::getCreateTime);
        Page<ExamRecord> recordPage = examRecordMapper.selectPage(page, wrapper);

        // 关联 exam_info 补齐考试标题/总分/及格分
        java.util.List<ExamRecordVO> voList = new java.util.ArrayList<>(recordPage.getRecords().size());
        for (ExamRecord r : recordPage.getRecords()) {
            ExamRecordVO vo = new ExamRecordVO();
            org.springframework.beans.BeanUtils.copyProperties(r, vo);
            ExamInfo exam = getById(r.getExamId());
            if (exam != null) {
                vo.setExamTitle(exam.getTitle());
                vo.setTotalScore(exam.getTotalScore());
                vo.setPassScore(exam.getPassScore());
            }
            voList.add(vo);
        }
        Page<ExamRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ExamRecord queryResult(String registerNo, String name) {
        // P0-07：成绩查询必须"准考证号 + 姓名"双因子匹配，杜绝仅凭序号遍历他人成绩
        if (registerNo == null || registerNo.isBlank() || name == null || name.isBlank()) {
            return null;
        }
        Long recordId;
        try {
            recordId = Long.parseLong(registerNo.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !Integer.valueOf(2).equals(record.getStatus())) {
            // 仅允许查询已交卷的成绩，进行中/草稿不对外
            return null;
        }
        User user = userMapper.selectById(record.getUserId());
        if (user == null) return null;
        String expected = user.getRealName() != null && !user.getRealName().isBlank()
                ? user.getRealName() : user.getNickname();
        if (expected == null || !expected.trim().equals(name.trim())) {
            return null; // 姓名不匹配，拒绝
        }
        return record;
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
    public List<ExamQuestionVO> listPaperQuestions(Long paperId, boolean includeAnswer) {
        List<ExamQuestion> questions = examQuestionMapper.selectByPaperId(paperId);
        List<ExamQuestionVO> result = new java.util.ArrayList<>(questions.size());
        for (ExamQuestion q : questions) {
            ExamQuestionVO vo = new ExamQuestionVO();
            vo.setId(q.getId());
            vo.setType(q.getType());
            vo.setTitle(q.getTitle());
            // P0-07：答题态绝不返回标准答案与解析，防止学生抓包作弊
            if (includeAnswer) {
                vo.setAnswer(q.getAnswer());
                vo.setAnalysis(q.getAnalysis());
            }
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

    @Override
    public List<ExamPaper> listPapers() {
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamPaper::getDeleted, 0).orderByDesc(ExamPaper::getCreateTime);
        return examPaperMapper.selectList(wrapper);
    }

    @Override
    public List<ExamResultDetailVO> getResultDetail(Long userId, Long recordId) {
        // 校验答卷归属，无权限直接抛异常
        ExamRecord record = getResult(userId, recordId);
        if (record.getPaperId() == null) {
            return java.util.Collections.emptyList();
        }
        // 试卷题目（含标准答案与解析，成绩页已交卷可见）
        List<ExamQuestionVO> questions = listPaperQuestions(record.getPaperId(), true);
        // 用户答案
        List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(recordId);
        java.util.Map<Long, ExamAnswer> answerMap = new java.util.HashMap<>();
        for (ExamAnswer a : answers) {
            answerMap.put(a.getQuestionId(), a);
        }

        List<ExamResultDetailVO> result = new java.util.ArrayList<>(questions.size());
        for (ExamQuestionVO q : questions) {
            ExamResultDetailVO vo = new ExamResultDetailVO();
            vo.setQuestionId(q.getId());
            vo.setType(q.getType());
            vo.setTitle(q.getTitle());
            vo.setOptions(q.getOptions());
            vo.setCorrectAnswer(q.getAnswer());
            vo.setAnalysis(q.getAnalysis());
            vo.setMaxScore(q.getScore());
            vo.setLanguage(q.getLanguage());

            ExamAnswer a = answerMap.get(q.getId());
            if (a != null) {
                vo.setUserAnswer(a.getUserAnswer());
                vo.setCode(a.getCode());
                vo.setScore(a.getScore());
                vo.setIsCorrect(a.getIsCorrect());
                vo.setGradeStatus(a.getGradeStatus());
            } else {
                vo.setUserAnswer("");
                vo.setScore(0);
                vo.setGradeStatus(0);
            }
            result.add(vo);
        }
        return result;
    }
}
