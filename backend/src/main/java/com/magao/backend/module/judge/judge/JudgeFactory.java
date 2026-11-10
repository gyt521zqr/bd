package com.magao.backend.module.judge.judge;

import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 判题工厂
 * <p>
 * 根据语言标识获取对应的判题实现类。
 * 新增编程语言时，只需新增 Judge 实现类并注册到工厂即可。
 * </p>
 *
 * @author Magao开发团队
 */
@Slf4j
@Component
public class JudgeFactory {

    /** 语言 -> 判题实现映射 */
    private final Map<String, AbstractJudge> judgeMap = new ConcurrentHashMap<>();

    /**
     * 自动注入所有 AbstractJudge 实现类
     */
    @Autowired
    public JudgeFactory(List<AbstractJudge> judges) {
        for (AbstractJudge judge : judges) {
            judgeMap.put(judge.getLang(), judge);
            log.info("注册判题引擎：{} -> {}", judge.getLang(), judge.getClass().getSimpleName());
        }
    }

    /**
     * 根据语言标识获取判题实现
     *
     * @param lang 语言标识
     * @return 对应的判题实现
     */
    public AbstractJudge getJudge(String lang) {
        AbstractJudge judge = judgeMap.get(lang);
        if (judge == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的语言类型：" + lang);
        }
        return judge;
    }

    /**
     * 获取所有支持的语言
     *
     * @return 支持的语言列表
     */
    public List<String> getSupportedLanguages() {
        return List.copyOf(judgeMap.keySet());
    }
}
