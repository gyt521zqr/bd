package com.magao.backend.module.competition.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.module.competition.entity.CompetitionParticipant;
import com.magao.backend.module.competition.entity.CompetitionSubmit;
import com.magao.backend.module.competition.mapper.CompetitionParticipantMapper;
import com.magao.backend.module.competition.mapper.CompetitionSubmitMapper;
import com.magao.backend.module.judge.dto.JudgeResultDTO;
import com.magao.backend.module.judge.dto.JudgeSubmitDTO;
import com.magao.backend.module.judge.judge.impl.ScratchJudge;
import com.magao.backend.mq.message.CompetitionJudgeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 竞赛判题消费者
 * <p>
 * 从 RabbitMQ 消费竞赛提交消息，调用 ScratchJudge 判题，
 * 将提交记录落库、更新参赛者最高分，并刷新 Redis 排行榜。
 * </p>
 *
 * @author Magao开发团队
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class CompetitionJudgeConsumer {

    private final ScratchJudge scratchJudge;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CompetitionSubmitMapper submitMapper;
    private final CompetitionParticipantMapper participantMapper;
    private final ObjectMapper objectMapper;

    /** 竞赛排行榜 SortedSet 前缀 */
    private static final String COMPETITION_RANKING_PREFIX = "competition:ranking:";

    @RabbitListener(queues = RabbitMQConstant.COMPETITION_JUDGE_QUEUE)
    public void handleCompetitionJudge(CompetitionJudgeMessage message) {
        log.info("收到竞赛判题消息：competitionId={}, userId={}", message.getCompetitionId(), message.getUserId());

        try {
            // 1. 构建判题参数
            JudgeSubmitDTO submitDTO = new JudgeSubmitDTO();
            submitDTO.setScratchJson(message.getSb3Data());
            submitDTO.setLang("scratch3");

            // 2. 调用 ScratchJudge 进行判题（已接入真实 Judge0 运行验证）
            JudgeResultDTO judgeResult = scratchJudge.judge(submitDTO);
            log.info("竞赛判题完成：competitionId={}, userId={}, status={}, score={}",
                    message.getCompetitionId(), message.getUserId(),
                    judgeResult.getStatus(), judgeResult.getScore());

            // 3. 保存提交记录到数据库
            saveSubmitRecord(message, judgeResult);

            // 4. 更新参赛者得分（取最高分）
            updateParticipantScore(message.getCompetitionId(), message.getUserId(),
                    judgeResult.getScore(), message.getSubmitTime());

            // 5. 更新 Redis 排行榜
            updateRanking(message.getCompetitionId(), message.getUserId(),
                    judgeResult.getScore() != null ? judgeResult.getScore() : 0);

            log.info("竞赛判题处理完成：competitionId={}, userId={}, score={}",
                    message.getCompetitionId(), message.getUserId(), judgeResult.getScore());
        } catch (Exception e) {
            log.error("竞赛判题处理异常：competitionId={}, userId={}",
                    message.getCompetitionId(), message.getUserId(), e);
        }
    }

    /**
     * 保存提交记录到数据库
     */
    private void saveSubmitRecord(CompetitionJudgeMessage message, JudgeResultDTO judgeResult) {
        try {
            CompetitionSubmit submit = new CompetitionSubmit();
            submit.setCompetitionId(message.getCompetitionId());
            submit.setUserId(message.getUserId());
            submit.setSb3Data(message.getSb3Data());
            // 将完整判分结果序列化为 JSON 落库（修复此前误存类名字符串的问题）
            submit.setJudgeResult(objectMapper.writeValueAsString(judgeResult));
            submit.setSubmitTime(message.getSubmitTime());
            submit.setIpAddress(message.getIpAddress());

            submitMapper.insert(submit);
            log.info("竞赛提交记录已保存：competitionId={}, userId={}, submitId={}",
                    message.getCompetitionId(), message.getUserId(), submit.getId());
        } catch (Exception e) {
            log.error("保存竞赛提交记录失败：competitionId={}, userId={}",
                    message.getCompetitionId(), message.getUserId(), e);
        }
    }

    /**
     * 更新参赛者得分（取历史最高分）
     */
    private void updateParticipantScore(Long competitionId, Long userId, Integer score, LocalDateTime submitTime) {
        int effectiveScore = (score != null) ? score : 0;
        try {
            LambdaQueryWrapper<CompetitionParticipant> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CompetitionParticipant::getCompetitionId, competitionId)
                    .eq(CompetitionParticipant::getUserId, userId);
            CompetitionParticipant participant = participantMapper.selectOne(wrapper);
            if (participant == null) {
                log.warn("参赛者记录不存在，跳过得分更新：competitionId={}, userId={}", competitionId, userId);
                return;
            }
            if (participant.getScore() == null || effectiveScore > participant.getScore()) {
                participant.setScore(effectiveScore);
                participant.setSubmitTime(submitTime);
                participantMapper.updateById(participant);
                log.info("参赛者得分已更新：competitionId={}, userId={}, score={}", competitionId, userId, effectiveScore);
            } else {
                log.info("参赛者已有更高分，保持不变：competitionId={}, userId={}, old={}, new={}",
                        competitionId, userId, participant.getScore(), effectiveScore);
            }
        } catch (Exception e) {
            log.error("更新参赛者得分失败：competitionId={}, userId={}", competitionId, userId, e);
        }
    }

    /**
     * 更新 Redis 排行榜（SortedSet）
     */
    private void updateRanking(Long competitionId, Long userId, Integer score) {
        try {
            String rankingKey = COMPETITION_RANKING_PREFIX + competitionId;
            redisTemplate.opsForZSet().add(rankingKey, String.valueOf(userId), score.doubleValue());
            redisTemplate.expire(rankingKey, java.time.Duration.ofDays(7));
            log.info("Redis排行榜已更新：competitionId={}, userId={}, score={}", competitionId, userId, score);
        } catch (Exception e) {
            log.error("更新Redis排行榜失败", e);
        }
    }
}
