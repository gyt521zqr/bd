package com.magao.backend.module.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.competition.entity.CompetitionInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞赛信息 Mapper
 *
 * @author Magao开发团队
 */
@Mapper
public interface CompetitionInfoMapper extends BaseMapper<CompetitionInfo> {
}
