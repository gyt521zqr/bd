package com.magao.backend.module.practice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.practice.entity.PracticeQuestion;
import com.magao.backend.module.practice.entity.PracticeRecord;

/**
 * 练习服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface PracticeService extends IService<PracticeQuestion> {

    /** 分页查询练习题列表 */
    Page<PracticeQuestion> pageList(PageDTO pageDTO, Integer language, Integer difficulty);

    /** 获取练习题详情 */
    PracticeQuestion getDetail(Long questionId);

    /** 提交代码评测 */
    PracticeRecord submit(Long userId, Long questionId, String code, Integer language);
}
