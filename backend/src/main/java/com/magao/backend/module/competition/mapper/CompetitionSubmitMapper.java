package com.magao.backend.module.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.competition.entity.CompetitionSubmit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞赛提交 Mapper
 * <p>
 * 注意：实际使用时需根据分表策略动态切换表名
 * </p>
 *
 * @author Magao开发团队
 */
@Mapper
public interface CompetitionSubmitMapper extends BaseMapper<CompetitionSubmit> {
}
