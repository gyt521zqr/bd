package com.magao.backend.module.competition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.competition.entity.CompetitionParticipant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞赛参赛者 Mapper
 *
 * @author Magao开发团队
 */
@Mapper
public interface CompetitionParticipantMapper extends BaseMapper<CompetitionParticipant> {
}
