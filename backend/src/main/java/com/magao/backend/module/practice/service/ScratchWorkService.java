package com.magao.backend.module.practice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.module.practice.entity.ScratchWork;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Scratch 作品服务接口
 * <p>
 * 提供 Scratch 自由创作作品的保存、加载、删除、导出等功能
 * </p>
 *
 * @author Magao开发团队
 */
public interface ScratchWorkService {

    /**
     * 保存 Scratch 作品
     * <p>
     * sb3 数据存 MinIO，元数据存 MySQL
     * </p>
     *
     * @param userId      用户ID
     * @param title       作品名称
     * @param description 作品描述
     * @param sb3File     sb3 文件
     * @param thumbnail   缩略图文件（可选）
     * @param isPublic    是否公开
     * @return 作品信息
     */
    ScratchWork saveWork(Long userId, String title, String description,
                         MultipartFile sb3File, MultipartFile thumbnail, Integer isPublic);

    /**
     * 加载用户作品列表（分页）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 作品分页列表
     */
    Page<ScratchWork> loadWorks(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 加载单个作品（从 MinIO 获取 sb3 数据）
     *
     * @param workId 作品ID
     * @param userId 用户ID（用于权限校验）
     * @return sb3 文件输入流
     */
    InputStream loadWork(Long workId, Long userId);

    /**
     * 获取作品元数据
     *
     * @param workId 作品ID
     * @return 作品信息
     */
    ScratchWork getWorkDetail(Long workId);

    /**
     * 删除作品（删除 MinIO 文件 + MySQL 记录）
     *
     * @param workId 作品ID
     * @param userId 用户ID（用于权限校验）
     */
    void deleteWork(Long workId, Long userId);

    /**
     * 导出作品（获取 sb3 文件的临时访问URL）
     *
     * @param workId 作品ID
     * @param userId 当前用户ID（归属校验，私有作品仅本人可导出）
     * @return sb3 文件临时访问URL
     */
    String exportWork(Long workId, Long userId);
}
