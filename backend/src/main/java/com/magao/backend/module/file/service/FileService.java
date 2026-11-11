package com.magao.backend.module.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.module.file.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface FileService extends IService<FileRecord> {

    /** 上传文件 */
    FileRecord upload(MultipartFile file, String bizType, Long bizId);

    /** 获取文件下载URL */
    String getDownloadUrl(Long fileId);

    /** 删除文件 */
    void deleteFile(Long fileId);

    /**
     * 获取文件输入流（统一回显/下载，本地磁盘与对象存储均适用）
     *
     * @param fileId 文件记录ID
     * @return 文件输入流
     */
    InputStream download(Long fileId);
}
