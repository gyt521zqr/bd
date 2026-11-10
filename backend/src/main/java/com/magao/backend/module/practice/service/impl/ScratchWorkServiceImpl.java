package com.magao.backend.module.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.util.TosUtil;
import com.magao.backend.module.practice.entity.ScratchWork;
import com.magao.backend.module.practice.mapper.ScratchWorkMapper;
import com.magao.backend.module.practice.service.ScratchWorkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Scratch 作品服务实现
 *
 * @author Magao开发团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScratchWorkServiceImpl implements ScratchWorkService {

    private final ScratchWorkMapper scratchWorkMapper;
    private final TosUtil tosUtil;

    /** TOS 存储桶名称 */
    private static final String SCRATCH_BUCKET = "scratch-works";

    /** sb3 文件存储文件夹 */
    private static final String SB3_FOLDER = "sb3/";

    /** 缩略图存储文件夹 */
    private static final String THUMBNAIL_FOLDER = "thumbnail/";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScratchWork saveWork(Long userId, String title, String description,
                               MultipartFile sb3File, MultipartFile thumbnail, Integer isPublic) {
        // 参数校验
        if (sb3File == null || sb3File.isEmpty()) {
            throw new BusinessException("sb3 文件不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("作品名称不能为空");
        }

        // 1. 上传 sb3 文件到 TOS
        String sb3FileKey = tosUtil.upload(sb3File, SCRATCH_BUCKET, SB3_FOLDER);
        log.info("sb3 文件上传成功：fileKey={}", sb3FileKey);

        // 2. 上传缩略图到 TOS（可选）
        String thumbnailKey = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailKey = tosUtil.upload(thumbnail, SCRATCH_BUCKET, THUMBNAIL_FOLDER);
            log.info("缩略图上传成功：fileKey={}", thumbnailKey);
        }

        // 3. 保存元数据到 MySQL
        ScratchWork work = new ScratchWork();
        work.setUserId(userId);
        work.setTitle(title.trim());
        work.setDescription(description != null ? description.trim() : "");
        work.setSb3FileKey(sb3FileKey);
        work.setThumbnailKey(thumbnailKey);
        work.setFileSize(sb3File.getSize());
        work.setIsPublic(isPublic != null ? isPublic : 0);
        work.setLikeCount(0);
        work.setViewCount(0);

        scratchWorkMapper.insert(work);
        log.info("Scratch 作品保存成功：id={}, userId={}, title={}", work.getId(), userId, title);

        return work;
    }

    @Override
    public Page<ScratchWork> loadWorks(Long userId, Integer pageNum, Integer pageSize) {
        Page<ScratchWork> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ScratchWork> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScratchWork::getUserId, userId)
                .orderByDesc(ScratchWork::getCreateTime);
        return scratchWorkMapper.selectPage(page, queryWrapper);
    }

    @Override
    public InputStream loadWork(Long workId, Long userId) {
        ScratchWork work = scratchWorkMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }

        // 权限校验：只能查看自己的作品或公开作品
        if (!work.getUserId().equals(userId) && work.getIsPublic() != 1) {
            throw new BusinessException("无权访问该作品");
        }

        // 从 TOS 获取 sb3 文件
        try {
            InputStream inputStream = tosUtil.download(work.getSb3FileKey(), SCRATCH_BUCKET);
            // 更新浏览数
            work.setViewCount(work.getViewCount() + 1);
            scratchWorkMapper.updateById(work);
            return inputStream;
        } catch (Exception e) {
            log.error("从 TOS 获取 sb3 文件失败：workId={}", workId, e);
            throw new BusinessException("获取作品文件失败：" + e.getMessage());
        }
    }

    @Override
    public ScratchWork getWorkDetail(Long workId) {
        ScratchWork work = scratchWorkMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }
        return work;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWork(Long workId, Long userId) {
        ScratchWork work = scratchWorkMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }

        // 权限校验：只能删除自己的作品
        if (!work.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该作品");
        }

        // 1. 删除 TOS 中的 sb3 文件
        try {
            tosUtil.delete(work.getSb3FileKey(), SCRATCH_BUCKET);
            log.info("TOS sb3 文件已删除：fileKey={}", work.getSb3FileKey());
        } catch (Exception e) {
            log.warn("删除 TOS sb3 文件失败：fileKey={}", work.getSb3FileKey(), e);
        }

        // 2. 删除 TOS 中的缩略图（如果存在）
        if (work.getThumbnailKey() != null && !work.getThumbnailKey().isEmpty()) {
            try {
                tosUtil.delete(work.getThumbnailKey(), SCRATCH_BUCKET);
                log.info("TOS 缩略图已删除：fileKey={}", work.getThumbnailKey());
            } catch (Exception e) {
                log.warn("删除 TOS 缩略图失败：fileKey={}", work.getThumbnailKey(), e);
            }
        }

        // 3. 删除 MySQL 记录
        scratchWorkMapper.deleteById(workId);
        log.info("Scratch 作品已删除：id={}, userId={}", workId, userId);
    }

    @Override
    public String exportWork(Long workId) {
        ScratchWork work = scratchWorkMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }

        // 生成临时访问URL（有效期1小时）
        return tosUtil.getPresignedUrl(work.getSb3FileKey(), SCRATCH_BUCKET, 3600);
    }
}
