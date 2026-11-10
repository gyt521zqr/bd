package com.magao.backend.module.growth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.growth.entity.PointLog;
import com.magao.backend.module.growth.entity.UserPoint;
import com.magao.backend.module.growth.vo.LeaderboardItemVO;
import com.magao.backend.module.growth.vo.LevelInfoVO;
import com.magao.backend.module.growth.vo.MedalItemVO;
import com.magao.backend.module.growth.vo.PointRecordVO;

import java.util.List;
import java.util.Map;

/**
 * 成长体系服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface GrowthService {

    /** 获取用户积分信息 */
    UserPoint getPointInfo(Long userId);

    /** 获取积分日志列表 */
    Page<PointLog> getPointLog(PageDTO pageDTO, Long userId);

    /** 增加积分 */
    void addPoint(Long userId, Integer point, Integer changeType, String description);

    /** 扣减积分 */
    void deductPoint(Long userId, Integer point, Integer changeType, String description);

    /** 积分概览（total/today/week/month），对齐前端 /growth/points */
    Map<String, Object> getPointsSummary(Long userId);

    /** 积分记录分页，对齐前端 /growth/points/records */
    Page<PointRecordVO> getPointRecords(PageDTO pageDTO, Long userId, String type);

    /** 勋章列表（含是否已获得），对齐前端 /growth/medals */
    List<MedalItemVO> getMedals(Long userId);

    /** 等级信息，对齐前端 /growth/level */
    LevelInfoVO getLevelInfo(Long userId);

    /** 排行榜，对齐前端 /growth/leaderboard */
    List<LeaderboardItemVO> getLeaderboard(String type, String period);

    /** 学习周报，对齐前端 /growth/weekly-report */
    Map<String, Object> getWeeklyReport(Long userId);
}
