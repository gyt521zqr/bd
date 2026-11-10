package com.magao.backend.module.judge.judge;

import com.magao.backend.module.judge.client.Judge0Client;
import com.magao.backend.module.judge.client.Judge0Result;
import com.magao.backend.module.judge.dto.JudgeResultDTO;
import com.magao.backend.module.judge.dto.JudgeSubmitDTO;
import com.magao.backend.module.judge.dto.RunResultDTO;
import com.magao.backend.module.practice.entity.PracticeQuestion;
import com.magao.backend.module.practice.entity.TestCase;
import com.magao.backend.module.practice.mapper.PracticeQuestionMapper;
import com.magao.backend.module.practice.mapper.TestCaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * 判题抽象类
 * <p>
 * 定义统一的判题方法与共用判分骨架：
 * - doJudge：基于题目测试用例（test_case 表）的真实判分（OJ 模式），逐用例提交 Judge0
 *   并以 expected_output 比对，聚合得分；
 * - doRun：仅运行代码查看输出（不判分），用于编辑器"运行"按钮；
 * - 子类只需提供语言标识、Judge0 数字语言 ID，以及可选的源码预处理（如 Scratch 转 Python）。
 * </p>
 *
 * @author Magao开发团队
 */
@Slf4j
public abstract class AbstractJudge {

    @Autowired
    protected Judge0Client judge0Client;
    @Autowired
    protected TestCaseMapper testCaseMapper;
    @Autowired
    protected PracticeQuestionMapper questionMapper;

    /**
     * 获取支持的语言标识（与 JudgeController 分发、JudgeFactory 注册一致）
     */
    public abstract String getLang();

    /**
     * 判题：运行所有测试用例，返回判题结果
     */
    public abstract JudgeResultDTO judge(JudgeSubmitDTO submitDTO);

    /**
     * 运行代码（不判题）
     */
    public abstract RunResultDTO run(JudgeSubmitDTO submitDTO);

    /**
     * 子类返回该语言在 Judge0 中的数字语言 ID（如 C++=54, Python=71）
     */
    public abstract int getJudge0LanguageId();

    /**
     * 源码预处理（默认原样返回；图形化语言可重写，如 Scratch 转 Python）
     */
    protected String preprocessSource(String sourceCode) {
        return sourceCode;
    }

    /**
     * 校验提交内容是否合法
     */
    public String validate(JudgeSubmitDTO submitDTO) {
        if (submitDTO.getQuestionId() == null) {
            return "题目ID不能为空";
        }
        return null;
    }

    /**
     * 基于测试用例的真实判分（OJ 模式）
     * <p>逐用例提交 Judge0（stdin=输入, expected_output=期望输出），由 Judge0 判定 AC/WA，
     * 聚合所有用例得出总分与状态。</p>
     */
    protected JudgeResultDTO doJudge(String rawSource, Long questionId, int languageId) {
        List<TestCase> cases = testCaseMapper.selectByQuestionId(questionId);
        if (cases == null || cases.isEmpty()) {
            return JudgeResultDTO.builder()
                    .status("SYSTEM_ERROR").score(0).totalScore(100)
                    .passedCases(0).totalCases(0)
                    .errorMessage("题目未配置测试用例，无法判分").build();
        }
        PracticeQuestion q = questionMapper.selectById(questionId);
        int timeLimit = (q != null && q.getTimeLimit() != null) ? q.getTimeLimit() : 1;
        int memLimit = (q != null && q.getMemoryLimit() != null) ? q.getMemoryLimit() : 256;

        String source = preprocessSource(rawSource);

        int total = cases.size();
        int passed = 0;
        long maxTime = 0;
        long maxMem = 0;
        String firstFailStatus = "ACCEPTED";
        String firstFailMsg = null;
        List<JudgeResultDTO.CaseResultDTO> caseResults = new ArrayList<>();

        for (TestCase tc : cases) {
            String stdin = tc.getInput() != null ? tc.getInput() : "";
            String expected = tc.getExpectedOutput();
            Judge0Result r = judge0Client.judge(source, languageId, stdin, expected, timeLimit, memLimit);
            if (r.isError()) {
                return JudgeResultDTO.builder()
                        .status("SYSTEM_ERROR").score(0).totalScore(100)
                        .passedCases(passed).totalCases(total)
                        .timeUsed(maxTime).memoryUsed(maxMem)
                        .errorMessage(r.getErrorMessage()).build();
            }
            String caseStatus = mapStatus(r.getStatusId());
            boolean ok = "ACCEPTED".equals(caseStatus);
            if (ok) {
                passed++;
            } else if ("ACCEPTED".equals(firstFailStatus)) {
                firstFailStatus = caseStatus;
                firstFailMsg = buildFailMessage(r);
            }
            if (r.getTime() != null) maxTime = Math.max(maxTime, (long) (r.getTime() * 1000));
            if (r.getMemory() != null) maxMem = Math.max(maxMem, r.getMemory());
            caseResults.add(JudgeResultDTO.CaseResultDTO.builder()
                    .caseId(tc.getId()).passed(ok)
                    .actualOutput(r.getStdout())
                    .expectedOutput(expected)
                    .timeUsed(r.getTime() == null ? null : (long) (r.getTime() * 1000))
                    .memoryUsed(r.getMemory())
                    .errorMessage(ok ? null : buildFailMessage(r))
                    .build());
        }

        int score = total > 0 ? passed * 100 / total : 0;
        String finalStatus = (passed == total) ? "ACCEPTED" : firstFailStatus;
        return JudgeResultDTO.builder()
                .status(finalStatus).score(score).totalScore(100)
                .passedCases(passed).totalCases(total)
                .timeUsed(maxTime).memoryUsed(maxMem)
                .caseResults(caseResults)
                .errorMessage(firstFailMsg)
                .build();
    }

    /**
     * 运行模式（不判分，仅执行并查看输出）
     */
    protected RunResultDTO doRun(String rawSource, int languageId, Long questionId, String stdin) {
        PracticeQuestion q = questionMapper.selectById(questionId);
        int timeLimit = (q != null && q.getTimeLimit() != null) ? q.getTimeLimit() : 1;
        int memLimit = (q != null && q.getMemoryLimit() != null) ? q.getMemoryLimit() : 256;
        String source = preprocessSource(rawSource);
        Judge0Result r = judge0Client.judge(source, languageId, stdin, null, timeLimit, memLimit);
        if (r.isError()) {
            return RunResultDTO.builder().success(false).errorMessage(r.getErrorMessage()).build();
        }
        boolean success = r.getStatusId() == 3 || r.getStatusId() == 4;
        StringBuilder out = new StringBuilder();
        if (r.getStdout() != null) out.append(r.getStdout());
        if (r.getStderr() != null && !r.getStderr().isEmpty()) {
            if (out.length() > 0) out.append("\n");
            out.append("[stderr]\n").append(r.getStderr());
        }
        return RunResultDTO.builder()
                .success(success)
                .output(out.length() > 0 ? out.toString() : (success ? "运行成功（无输出）" : r.getStatusDescription()))
                .timeUsed(r.getTime() == null ? null : (long) (r.getTime() * 1000))
                .build();
    }

    /**
     * 将 Judge0 状态码映射为系统统一判分状态字符串
     */
    protected String mapStatus(int id) {
        return switch (id) {
            case 3 -> "ACCEPTED";
            case 4 -> "WRONG_ANSWER";
            case 5 -> "TIME_LIMIT_EXCEEDED";
            case 6 -> "COMPILE_ERROR";
            case 7, 8 -> "RUNTIME_ERROR";
            case 13 -> "MEMORY_LIMIT_EXCEEDED";
            default -> "SYSTEM_ERROR";
        };
    }

    /**
     * 组装用例失败信息（编译错误/运行错误详情）
     */
    protected String buildFailMessage(Judge0Result r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.getStatusDescription() == null ? "执行失败" : r.getStatusDescription());
        if (r.getCompileOutput() != null && !r.getCompileOutput().isEmpty()) {
            sb.append("\n").append(r.getCompileOutput());
        }
        if (r.getStderr() != null && !r.getStderr().isEmpty()) {
            sb.append("\n").append(r.getStderr());
        }
        if (r.getMessage() != null && !r.getMessage().isEmpty()) {
            sb.append("\n").append(r.getMessage());
        }
        return sb.toString().trim();
    }
}
