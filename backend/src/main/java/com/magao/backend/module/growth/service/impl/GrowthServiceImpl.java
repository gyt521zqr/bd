package com.magao.backend.module.growth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.growth.entity.Medal;
import com.magao.backend.module.growth.entity.PointLog;
import com.magao.backend.module.growth.entity.UserLevel;
import com.magao.backend.module.growth.entity.UserMedal;
import com.magao.backend.module.growth.entity.UserPoint;
import com.magao.backend.module.growth.mapper.MedalMapper;
import com.magao.backend.module.growth.mapper.PointLogMapper;
import com.magao.backend.module.growth.mapper.UserLevelMapper;
import com.magao.backend.module.growth.mapper.UserMedalMapper;
import com.magao.backend.module.growth.mapper.UserPointMapper;
import com.magao.backend.module.growth.service.GrowthService;
import com.magao.backend.module.growth.vo.LeaderboardItemVO;
import com.magao.backend.module.growth.vo.LevelInfoVO;
import com.magao.backend.module.growth.vo.MedalItemVO;
import com.magao.backend.module.growth.vo.PointRecordVO;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 成长体系服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrowthServiceImpl implements GrowthService {

    private final UserPointMapper userPointMapper;
    private final PointLogMapper pointLogMapper;
    private final MedalMapper medalMapper;
    private final UserMedalMapper userMedalMapper;
    private final UserLevelMapper userLevelMapper;
    private final UserMapper userMapper;

    @Override
    public UserPoint getPointInfo(Long userId) {
        return userPointMapper.selectOne(new LambdaQueryWrapper<UserPoint>().eq(UserPoint::getUserId, userId));
    }

    @Override
    public Page<PointLog> getPointLog(PageDTO pageDTO, Long userId) {
        Page<PointLog> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<PointLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointLog::getUserId, userId).orderByDesc(PointLog::getCreateTime);
        return pointLogMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoint(Long userId, Integer point, Integer changeType, String description) {
        UserPoint userPoint = getPointInfo(userId);
        if (userPoint == null) {
            userPoint = new UserPoint();
            userPoint.setUserId(userId);
            userPoint.setTotalPoint(0);
            userPoint.setTotalEarned(0);
            userPoint.setTotalSpent(0);
            userPointMapper.insert(userPoint);
        }
        userPoint.setTotalPoint(userPoint.getTotalPoint() + point);
        userPoint.setTotalEarned(userPoint.getTotalEarned() + point);
        userPointMapper.updateById(userPoint);
        PointLog log = new PointLog();
        log.setUserId(userId);
        log.setChangePoint(point);
        log.setAfterPoint(userPoint.getTotalPoint());
        log.setChangeType(changeType);
        log.setDescription(description);
        pointLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoint(Long userId, Integer point, Integer changeType, String description) {
        UserPoint userPoint = getPointInfo(userId);
        if (userPoint == null || userPoint.getTotalPoint() < point) {
            throw new BusinessException(ErrorCode.BALANCE_NOT_ENOUGH);
        }
        userPoint.setTotalPoint(userPoint.getTotalPoint() - point);
        userPoint.setTotalSpent(userPoint.getTotalSpent() + point);
        userPointMapper.updateById(userPoint);
        PointLog log = new PointLog();
        log.setUserId(userId);
        log.setChangePoint(-point);
        log.setAfterPoint(userPoint.getTotalPoint());
        log.setChangeType(changeType);
        log.setDescription(description);
        pointLogMapper.insert(log);
    }

    @Override
    public Map<String, Object> getPointsSummary(Long userId) {
        UserPoint up = getPointInfo(userId);
        int total = up == null || up.getTotalPoint() == null ? 0 : up.getTotalPoint();
        LocalDate today = LocalDate.now();
        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime startWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime startMonth = today.withDayOfMonth(1).atStartOfDay();
        int todayP = pointLogMapper.sumChangePointSince(userId, startToday);
        int weekP = pointLogMapper.sumChangePointSince(userId, startWeek);
        int monthP = pointLogMapper.sumChangePointSince(userId, startMonth);
        Map<String, Object> m = new HashMap<>();
        m.put("total", total);
        m.put("today", todayP);
        m.put("week", weekP);
        m.put("month", monthP);
        return m;
    }

    @Override
    public Page<PointRecordVO> getPointRecords(PageDTO pageDTO, Long userId, String type) {
        Page<PointLog> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<PointLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointLog::getUserId, userId);
        Integer ct = parseType(type);
        if (ct != null) {
            wrapper.eq(PointLog::getChangeType, ct);
        }
        wrapper.orderByDesc(PointLog::getCreateTime);
        Page<PointLog> res = pointLogMapper.selectPage(page, wrapper);
        Page<PointRecordVO> out = new Page<>(res.getCurrent(), res.getSize(), res.getTotal());
        out.setRecords(res.getRecords().stream().map(this::toRecordVO).collect(Collectors.toList()));
        return out;
    }

    @Override
    public List<MedalItemVO> getMedals(Long userId) {
        List<Medal> all = medalMapper.selectList(new LambdaQueryWrapper<Medal>().orderByAsc(Medal::getSortOrder));
        Set<Long> owned = userMedalMapper
                .selectList(new LambdaQueryWrapper<UserMedal>().eq(UserMedal::getUserId, userId))
                .stream().map(UserMedal::getMedalId).collect(Collectors.toSet());
        List<MedalItemVO> out = new ArrayList<>();
        for (Medal m : all) {
            MedalItemVO v = new MedalItemVO();
            v.setId(m.getId());
            v.setName(m.getMedalName());
            v.setIcon(m.getIconUrl());
            v.setDescription(m.getDescription());
            v.setObtained(owned.contains(m.getId()));
            v.setCondition(m.getConditionType() + ":" + m.getConditionValue());
            out.add(v);
        }
        return out;
    }

    @Override
    public LevelInfoVO getLevelInfo(Long userId) {
        UserPoint up = getPointInfo(userId);
        int current = up == null || up.getTotalPoint() == null ? 0 : up.getTotalPoint();
        UserLevel ul = userLevelMapper.selectOne(new LambdaQueryWrapper<UserLevel>().eq(UserLevel::getUserId, userId));
        LevelInfoVO v = new LevelInfoVO();
        v.setIcon("");
        v.setCurrentPoints(current);
        if (ul != null) {
            v.setLevel(ul.getLevel());
            v.setName(ul.getLevelName());
            int exp = ul.getExperience() == null ? 0 : ul.getExperience();
            v.setMinPoints(Math.max(0, current - exp));
            v.setMaxPoints(ul.getNextLevelExp() == null ? current + 100 : ul.getNextLevelExp());
        } else {
            v.setLevel(1);
            v.setName("初学者");
            v.setMinPoints(0);
            v.setMaxPoints(100);
        }
        return v;
    }

    @Override
    public List<LeaderboardItemVO> getLeaderboard(String type, String period) {
        int limit = "all".equals(period) ? 100 : 50;
        Page<UserPoint> page = new Page<>(1, limit);
        Page<UserPoint> res = userPointMapper
                .selectPage(page, new LambdaQueryWrapper<UserPoint>().orderByDesc(UserPoint::getTotalPoint));
        List<LeaderboardItemVO> out = new ArrayList<>();
        int rank = 0;
        for (UserPoint up : res.getRecords()) {
            rank++;
            LeaderboardItemVO v = new LeaderboardItemVO();
            v.setRank(rank);
            v.setUserId(up.getUserId());
            User u = userMapper.selectById(up.getUserId());
            v.setNickname(u == null ? ("用户" + up.getUserId()) : (u.getNickname() == null ? u.getUsername() : u.getNickname()));
            v.setAvatar(u == null ? null : u.getAvatar());
            v.setPoints(up.getTotalPoint() == null ? 0 : up.getTotalPoint());
            UserLevel ul = userLevelMapper.selectOne(new LambdaQueryWrapper<UserLevel>().eq(UserLevel::getUserId, up.getUserId()));
            v.setLevel(ul == null ? 1 : ul.getLevel());
            v.setSubmitCount(0);
            out.add(v);
        }
        return out;
    }

    @Override
    public Map<String, Object> getWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startWeek = today.with(DayOfWeek.MONDAY).atStartOfDay();
        Map<String, Object> m = new HashMap<>();
        m.put("weekStart", startWeek.toLocalDate().toString());
        m.put("weekEnd", startWeek.plusDays(6).toLocalDate().toString());
        m.put("totalPoints", pointLogMapper.sumChangePointSince(userId, startWeek));
        m.put("studyDays", pointLogMapper.countStudyDaysSince(userId, startWeek));
        m.put("completedExams", 0);
        m.put("completedCourses", 0);
        return m;
    }

    private PointRecordVO toRecordVO(PointLog log) {
        PointRecordVO v = new PointRecordVO();
        v.setId(log.getId());
        v.setPoints(log.getChangePoint());
        v.setType(typeName(log.getChangeType()));
        v.setDescription(log.getDescription());
        v.setCreateTime(log.getCreateTime());
        return v;
    }

    private Integer parseType(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        switch (type) {
            case "签到": return 1;
            case "学习": return 2;
            case "考试": return 3;
            case "兑换": return 4;
            case "调整": return 5;
            default: return null;
        }
    }

    private String typeName(Integer changeType) {
        if (changeType == null) return "其他";
        switch (changeType) {
            case 1: return "签到";
            case 2: return "学习";
            case 3: return "考试";
            case 4: return "兑换";
            case 5: return "调整";
            default: return "其他";
        }
    }
}
