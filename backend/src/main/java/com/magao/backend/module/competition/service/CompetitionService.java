package com.magao.backend.module.competition.service;

import com.magao.backend.module.competition.dto.CompetitionCreateDTO;
import com.magao.backend.module.competition.dto.CompetitionRankingDTO;
import com.magao.backend.module.competition.dto.CompetitionSubmitDTO;
import com.magao.backend.module.competition.entity.CompetitionInfo;

import java.util.List;

/**
 * 竞赛服务接口
 *
 * @author Magao开发团队
 */
public interface CompetitionService {

    /**
     * 创建竞赛
     *
     * @param createDTO 创建竞赛参数
     * @param creatorId 创建者ID
     * @return 竞赛信息
     */
    CompetitionInfo createCompetition(CompetitionCreateDTO createDTO, Long creatorId);

    /**
     * 加入竞赛（Redis计数+防重复）
     *
     * @param competitionId 竞赛ID
     * @param userId        用户ID
     */
    void joinCompetition(Long competitionId, Long userId);

    /**
     * 提交作品（RabbitMQ异步判题）
     *
     * @param submitDTO 提交参数
     * @param userId    用户ID
     * @param ipAddress 提交IP地址
     */
    void submitWork(CompetitionSubmitDTO submitDTO, Long userId, String ipAddress);

    /**
     * 获取实时排行榜（Redis SortedSet）
     *
     * @param competitionId 竞赛ID
     * @return 排行榜列表
     */
    List<CompetitionRankingDTO> getRanking(Long competitionId);

    /**
     * 获取竞赛状态
     *
     * @param competitionId 竞赛ID
     * @return 竞赛信息
     */
    CompetitionInfo getCompetitionStatus(Long competitionId);

    /**
     * 获取竞赛详情
     *
     * @param competitionId 竞赛ID
     * @return 竞赛信息
     */
    CompetitionInfo getCompetitionDetail(Long competitionId);

    /**
     * 分页查询竞赛列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   竞赛状态（可选）
     * @return 竞赛列表
     */
    List<CompetitionInfo> listCompetitions(Integer pageNum, Integer pageSize, Integer status);
}
