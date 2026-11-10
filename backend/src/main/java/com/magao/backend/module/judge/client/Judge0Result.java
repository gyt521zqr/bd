package com.magao.backend.module.judge.client;

import lombok.Data;

/**
 * Judge0 单次判题返回结果
 *
 * @author Magao开发团队
 */
@Data
public class Judge0Result {

    /** 状态码（Judge0 标准：3=AC, 4=WA, 5=TLE, 6=CE, 7/8=RE, 13=MLE ...） */
    private int statusId;

    /** 状态描述 */
    private String statusDescription;

    /** 标准输出（已解码为原文） */
    private String stdout;

    /** 标准错误（已解码为原文） */
    private String stderr;

    /** 编译错误输出（已解码为原文） */
    private String compileOutput;

    /** 附加信息（如运行错误信息） */
    private String message;

    /** 运行时间（秒） */
    private Double time;

    /** 内存消耗（KB） */
    private Long memory;

    /** 退出码 */
    private Integer exitCode;

    /** 是否发生系统/连接错误（非代码本身问题） */
    private boolean error;

    /** 错误信息（error=true 时有效） */
    private String errorMessage;

    /**
     * 构造一个系统错误结果
     */
    public static Judge0Result error(String msg) {
        Judge0Result r = new Judge0Result();
        r.error = true;
        r.errorMessage = msg;
        return r;
    }
}
