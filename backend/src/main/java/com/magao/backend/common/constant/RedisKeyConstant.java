package com.magao.backend.common.constant;

/**
 * Redis Key前缀常量类
 * <p>
 * 统一管理Redis中所有Key的前缀，避免Key冲突，
 * 便于管理和维护Redis中的数据。
 * </p>
 * <p>
 * 命名规范：模块:功能:标识
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public class RedisKeyConstant {

    /** 私有构造方法，防止实例化 */
    private RedisKeyConstant() {
    }

    // ==================== 用户模块 ====================

    /** 用户Token前缀（格式：user:token:{userId}） */
    public static final String USER_TOKEN_PREFIX = "user:token:";
    /** 用户登录信息前缀（格式：user:info:{userId}） */
    public static final String USER_INFO_PREFIX = "user:info:";
    /** 用户短信验证码前缀（格式：user:sms:{phone}） */
    public static final String USER_SMS_CODE_PREFIX = "user:sms:";
    /** 用户登录失败次数前缀（格式：user:login:fail:{username}） */
    public static final String USER_LOGIN_FAIL_PREFIX = "user:login:fail:";
    /** 用户在线状态前缀（格式：user:online:{userId}） */
    public static final String USER_ONLINE_PREFIX = "user:online:";
    /** 用户权限缓存前缀（格式：user:perms:{userId}） */
    public static final String USER_PERMS_PREFIX = "user:perms:";

    // ==================== 限流模块 ====================

    /** 接口限流前缀（格式：rate:limit:{接口标识}） */
    public static final String RATE_LIMIT_PREFIX = "rate:limit:";
    /** IP限流前缀（格式：rate:ip:{ip地址}） */
    public static final String RATE_IP_PREFIX = "rate:ip:";
    /** 防重复提交前缀（格式：duplicate:{md5(请求参数)}） */
    public static final String DUPLICATE_SUBMIT_PREFIX = "duplicate:";

    // ==================== 考试模块 ====================

    /** 考试缓存前缀（格式：exam:info:{examId}） */
    public static final String EXAM_INFO_PREFIX = "exam:info:";
    /** 考试答卷缓存前缀（格式：exam:answer:{recordId}） */
    public static final String EXAM_ANSWER_PREFIX = "exam:answer:";
    /** 考试防作弊快照前缀（格式：exam:snapshot:{recordId}） */
    public static final String EXAM_SNAPSHOT_PREFIX = "exam:snapshot:";
    /** 考试倒计时前缀（格式：exam:countdown:{recordId}） */
    public static final String EXAM_COUNTDOWN_PREFIX = "exam:countdown:";

    // ==================== 课程模块 ====================

    /** 课程缓存前缀（格式：course:info:{courseId}） */
    public static final String COURSE_INFO_PREFIX = "course:info:";
    /** 课程学习进度前缀（格式：course:progress:{userId}:{courseId}） */
    public static final String COURSE_PROGRESS_PREFIX = "course:progress:";

    // ==================== 代码评测模块 ====================

    /** 评测结果缓存前缀（格式：judge:result:{submissionId}） */
    public static final String JUDGE_RESULT_PREFIX = "judge:result:";

    // ==================== 竞赛模块 ====================

    /** 竞赛信息缓存前缀（格式：competition:info:{competitionId}） */
    public static final String COMPETITION_INFO_PREFIX = "competition:info:";
    /** 竞赛参赛人数计数前缀（格式：competition:join_count:{competitionId}） */
    public static final String COMPETITION_JOIN_COUNT_PREFIX = "competition:join_count:";
    /** 竞赛已参加用户集合前缀（格式：competition:joined_users:{competitionId}） */
    public static final String COMPETITION_JOINED_USERS_PREFIX = "competition:joined_users:";
    /** 竞赛排行榜前缀（格式：competition:ranking:{competitionId}） */
    public static final String COMPETITION_RANKING_PREFIX = "competition:ranking:";
    /** 竞赛分布式锁前缀（格式：lock:competition:submit:{competitionId}:{userId}） */
    public static final String COMPETITION_SUBMIT_LOCK_PREFIX = "lock:competition:submit:";

    // ==================== 通用缓存 ====================

    /** 分布式锁前缀（格式：lock:{业务标识}） */
    public static final String LOCK_PREFIX = "lock:";
    /** 验证码前缀（格式：captcha:{uuid}） */
    public static final String CAPTCHA_PREFIX = "captcha:";
    /** 字典缓存前缀（格式：dict:{dictType}） */
    public static final String DICT_PREFIX = "dict:";
    /** 配置缓存前缀（格式：config:{configKey}） */
    public static final String CONFIG_PREFIX = "config:";

    // ==================== 过期时间（秒） ====================

    /** Token过期时间：2小时 */
    public static final long TOKEN_EXPIRE_SECONDS = 7200;
    /** 短信验证码过期时间：5分钟 */
    public static final long SMS_CODE_EXPIRE_SECONDS = 300;
    /** 验证码过期时间：2分钟 */
    public static final long CAPTCHA_EXPIRE_SECONDS = 120;
    /** 防重复提交过期时间：10秒 */
    public static final long DUPLICATE_SUBMIT_EXPIRE_SECONDS = 10;
    /** 临时缓存过期时间：30分钟 */
    public static final long TEMP_EXPIRE_SECONDS = 1800;
}
