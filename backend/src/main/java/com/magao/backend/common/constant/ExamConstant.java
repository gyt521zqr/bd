package com.magao.backend.common.constant;

/**
 * 考试相关常量类
 * <p>
 * 定义考试模块中使用的常量值，包括：
 * - 考试状态
 * - 题目类型
 * - 评分规则
 * - 防作弊相关参数
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public class ExamConstant {

    /** 私有构造方法，防止实例化 */
    private ExamConstant() {
    }

    // ==================== 考试状态 ====================

    /** 考试状态：未开始 */
    public static final Integer EXAM_STATUS_NOT_STARTED = 0;
    /** 考试状态：进行中 */
    public static final Integer EXAM_STATUS_IN_PROGRESS = 1;
    /** 考试状态：已结束 */
    public static final Integer EXAM_STATUS_FINISHED = 2;
    /** 考试状态：已批改 */
    public static final Integer EXAM_STATUS_GRADED = 3;

    // ==================== 答题状态 ====================

    /** 答题状态：未作答 */
    public static final Integer ANSWER_STATUS_UNANSWERED = 0;
    /** 答题状态：已作答 */
    public static final Integer ANSWER_STATUS_ANSWERED = 1;
    /** 答题状态：已批改 */
    public static final Integer ANSWER_STATUS_GRADED = 2;

    // ==================== 防作弊参数 ====================

    /** 违规检测：切屏次数上限 */
    public static final Integer MAX_SWITCH_SCREEN_COUNT = 5;
    /** 违规检测：离开页面次数上限 */
    public static final Integer MAX_LEAVE_PAGE_COUNT = 3;
    /** 违规检测：最小答题时间（秒），低于此时间视为异常 */
    public static final Integer MIN_ANSWER_TIME_SECONDS = 10;
    /** 违规检测：快照间隔时间（秒） */
    public static final Integer SNAPSHOT_INTERVAL_SECONDS = 30;

    // ==================== 评分相关 ====================

    /** 编程题自动评分：满分 */
    public static final Integer AUTO_GRADE_FULL_SCORE = 100;
    /** 编程题自动评分：通过一个测试用例得分 */
    public static final Integer AUTO_GRADE_PER_CASE = 0;

    // ==================== 考试时间 ====================

    /** 考试提前进入时间（分钟） */
    public static final Integer EARLY_ENTER_MINUTES = 5;
    /** 考试延迟交卷时间（分钟） */
    public static final Integer LATE_SUBMIT_MINUTES = 0;
}
