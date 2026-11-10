package com.magao.backend.module.competition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.common.constant.RedisKeyConstant;
import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.util.RedisUtil;
import com.magao.backend.module.competition.dto.CompetitionCreateDTO;
import com.magao.backend.module.competition.dto.CompetitionRankingDTO;
import com.magao.backend.module.competition.dto.CompetitionSubmitDTO;
import com.magao.backend.module.competition.entity.CompetitionInfo;
import com.magao.backend.module.competition.entity.CompetitionParticipant;
import com.magao.backend.module.competition.mapper.CompetitionInfoMapper;
import com.magao.backend.module.competition.mapper.CompetitionParticipantMapper;
import com.magao.backend.module.competition.mapper.CompetitionSubmitMapper;
import com.magao.backend.module.competition.service.CompetitionService;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.mapper.UserMapper;
import com.magao.backend.mq.message.CompetitionJudgeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 竞赛服务实现
 *
 * @author Magao开发团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionInfoMapper competitionInfoMapper;
    private final CompetitionParticipantMapper participantMapper;
    private final CompetitionSubmitMapper submitMapper;
    private final RedisUtil redisUtil;
    private final RabbitTemplate rabbitTemplate;
    private final UserMapper userMapper;

    // ==================== Redis Key 前缀 ====================

    /** 竞赛参赛人数计数前缀（格式：competition:join_count:{competitionId}） */
    private static final String COMPETITION_JOIN_COUNT_PREFIX = "competition:join_count:";
    /** 竞赛已参加用户集合前缀（格式：competition:joined_users:{competitionId}） */
    private static final String COMPETITION_JOINED_USERS_PREFIX = "competition:joined_users:";
    /** 竞赛排行榜 SortedSet 前缀（格式：competition:ranking:{competitionId}） */
    private static final String COMPETITION_RANKING_PREFIX = "competition:ranking:";
    /** 竞赛信息缓存前缀（格式：competition:info:{competitionId}） */
    private static final String COMPETITION_INFO_PREFIX = "competition:info:";
    /** 竞赛分布式锁前缀（格式：lock:competition:submit:{competitionId}:{userId}） */
    private static final String COMPETITION_SUBMIT_LOCK_PREFIX = "lock:competition:submit:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionInfo createCompetition(CompetitionCreateDTO createDTO, Long creatorId) {
        // 参数校验
        if (createDTO.getStartTime().isAfter(createDTO.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        CompetitionInfo competition = new CompetitionInfo();
        competition.setTitle(createDTO.getTitle());
        competition.setDescription(createDTO.getDescription());
        competition.setStartTime(createDTO.getStartTime());
        competition.setEndTime(createDTO.getEndTime());
        competition.setStatus(0); // 未开始
        competition.setAllowedBlockCategories(createDTO.getAllowedBlockCategories());
        competition.setMaxParticipants(createDTO.getMaxParticipants() != null ? createDTO.getMaxParticipants() : 500);
        competition.setCreatorId(creatorId);

        competitionInfoMapper.insert(competition);

        // 初始化 Redis 排行榜
        String rankingKey = COMPETITION_RANKING_PREFIX + competition.getId();
        redisUtil.set(rankingKey, 0, 30, java.util.concurrent.TimeUnit.DAYS);

        // 缓存竞赛信息
        redisUtil.set(COMPETITION_INFO_PREFIX + competition.getId(), competition,
                RedisKeyConstant.TEMP_EXPIRE_SECONDS, java.util.concurrent.TimeUnit.SECONDS);

        log.info("创建竞赛成功：id={}, title={}", competition.getId(), competition.getTitle());
        return competition;
    }

    @Override
    public void joinCompetition(Long competitionId, Long userId) {
        // 1. 检查竞赛是否存在且进行中
        CompetitionInfo competition = getCompetitionDetail(competitionId);
        if (competition == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (competition.getStatus() != 1) {
            throw new BusinessException("竞赛未在进行中，无法加入");
        }

        // 2. Redis 防重复加入（Set 判断）
        String joinedUsersKey = COMPETITION_JOINED_USERS_PREFIX + competitionId;
        Boolean isMember = redisUtil.hGet(joinedUsersKey, String.valueOf(userId)) != null;
        if (Boolean.TRUE.equals(isMember)) {
            throw new BusinessException("您已加入该竞赛，请勿重复加入");
        }

        // 3. Redis 计数 + 人数限制检查
        String countKey = COMPETITION_JOIN_COUNT_PREFIX + competitionId;
        Long currentCount = redisUtil.increment(countKey);
        if (currentCount != null && currentCount > competition.getMaxParticipants()) {
            redisUtil.increment(countKey, -1); // 回退计数
            throw new BusinessException("竞赛人数已满");
        }

        // 4. 记录参赛者到 MySQL
        CompetitionParticipant participant = new CompetitionParticipant();
        participant.setCompetitionId(competitionId);
        participant.setUserId(userId);
        participant.setJoinTime(LocalDateTime.now());
        participant.setScore(0);
        participant.setRanking(0);
        participant.setViolationCount(0);
        participantMapper.insert(participant);

        // 5. 记录到 Redis Set 防重复
        redisUtil.hSet(joinedUsersKey, String.valueOf(userId), String.valueOf(participant.getId()));

        log.info("用户加入竞赛成功：competitionId={}, userId={}", competitionId, userId);
    }

    @Override
    public void submitWork(CompetitionSubmitDTO submitDTO, Long userId, String ipAddress) {
        Long competitionId = submitDTO.getCompetitionId();

        // 1. 检查竞赛状态
        CompetitionInfo competition = getCompetitionDetail(competitionId);
        if (competition == null) {
            throw new BusinessException("竞赛不存在");
        }
        if (competition.getStatus() != 1) {
            throw new BusinessException("竞赛未在进行中，无法提交");
        }

        // 2. 检查是否已参赛
        String joinedUsersKey = COMPETITION_JOINED_USERS_PREFIX + competitionId;
        Object joined = redisUtil.hGet(joinedUsersKey, String.valueOf(userId));
        if (joined == null) {
            throw new BusinessException("请先加入竞赛再提交作品");
        }

        // 3. 分布式锁防止重复提交
        String lockKey = COMPETITION_SUBMIT_LOCK_PREFIX + competitionId + ":" + userId;
        Boolean locked = redisUtil.setIfAbsent(lockKey, "1", 60, java.util.concurrent.TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException("请勿频繁提交，请稍后再试");
        }

        try {
            // 4. 发送消息到 RabbitMQ 异步判题
            CompetitionJudgeMessage message = new CompetitionJudgeMessage();
            message.setCompetitionId(competitionId);
            message.setUserId(userId);
            message.setSb3Data(submitDTO.getSb3Data());
            message.setIpAddress(ipAddress);
            message.setSubmitTime(LocalDateTime.now());

            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.COMPETITION_JUDGE_EXCHANGE,
                    RabbitMQConstant.COMPETITION_JUDGE_ROUTING_KEY,
                    message);

            log.info("竞赛作品提交成功，已发送判题消息：competitionId={}, userId={}", competitionId, userId);
        } finally {
            // 释放锁
            redisUtil.delete(lockKey);
        }
    }

    @Override
    public List<CompetitionRankingDTO> getRanking(Long competitionId) {
        // 简化实现：直接从数据库查询排行榜（按分数降序）
        LambdaQueryWrapper<CompetitionParticipant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompetitionParticipant::getCompetitionId, competitionId)
                .gt(CompetitionParticipant::getScore, 0)
                .orderByDesc(CompetitionParticipant::getScore)
                .last("LIMIT 100");

        List<CompetitionParticipant> participants = participantMapper.selectList(queryWrapper);

        // 批量查询用户信息
        List<Long> userIds = participants.stream()
                .map(CompetitionParticipant::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }

        // 构建排行榜数据
        List<CompetitionRankingDTO> ranking = new ArrayList<>();
        int rank = 1;
        for (CompetitionParticipant p : participants) {
            User user = userMap.get(p.getUserId());
            ranking.add(CompetitionRankingDTO.builder()
                    .ranking(rank++)
                    .userId(p.getUserId())
                    .username(user != null ? user.getUsername() : null)
                    .avatar(user != null ? user.getAvatar() : null)
                    .score(p.getScore())
                    .submitTime(p.getSubmitTime() != null ? p.getSubmitTime().toString() : null)
                    .build());
        }

        return ranking;
    }

    @Override
    public CompetitionInfo getCompetitionStatus(Long competitionId) {
        CompetitionInfo competition = getCompetitionDetail(competitionId);
        if (competition == null) {
            throw new BusinessException("竞赛不存在");
        }

        // 自动更新竞赛状态
        LocalDateTime now = LocalDateTime.now();
        if (competition.getStatus() == 0 && now.isAfter(competition.getStartTime()) && now.isBefore(competition.getEndTime())) {
            competition.setStatus(1); // 进行中
            competitionInfoMapper.updateById(competition);
        } else if (competition.getStatus() == 1 && now.isAfter(competition.getEndTime())) {
            competition.setStatus(2); // 已结束
            competitionInfoMapper.updateById(competition);
        }

        return competition;
    }

    @Override
    public CompetitionInfo getCompetitionDetail(Long competitionId) {
        // 先从缓存获取
        Object cached = redisUtil.get(COMPETITION_INFO_PREFIX + competitionId);
        if (cached instanceof CompetitionInfo) {
            return (CompetitionInfo) cached;
        }

        // 从数据库获取
        CompetitionInfo competition = competitionInfoMapper.selectById(competitionId);
        if (competition != null) {
            redisUtil.set(COMPETITION_INFO_PREFIX + competitionId, competition,
                    RedisKeyConstant.TEMP_EXPIRE_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
        }
        return competition;
    }

    @Override
    public List<CompetitionInfo> listCompetitions(Integer pageNum, Integer pageSize, Integer status) {
        LambdaQueryWrapper<CompetitionInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(CompetitionInfo::getStatus, status);
        }
        queryWrapper.orderByDesc(CompetitionInfo::getCreateTime)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);

        return competitionInfoMapper.selectList(queryWrapper);
    }
}
