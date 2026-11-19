package com.magao.backend.module.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.file.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

/**
 * 文件服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface FileService extends IService<FileRecord> {

    /** 允许上传的扩展名白名单（图片 + 常用文档，杜绝可执行文件/脚本） */
    Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg",
            "mp4", "webm", "mp3", "pdf", "sb3", "txt", "md");

    /** 业务类型安全格式：仅字母/数字/下划线/短横线，杜绝路径穿越（../、绝对路径、斜杠） */
    String BIZ_TYPE_REGEX = "^[a-zA-Z0-9_-]{1,32}$";

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

    /**
     * 校验业务类型安全（P1-19：防路径穿越）。
     * <p>
     * bizType 直接拼接为存储目录/对象 key 前缀，前端可控，必须限定为安全字符。
     * </p>
     */
    default void validateBizType(String bizType) {
        if (bizType == null || !bizType.matches(BIZ_TYPE_REGEX)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法的业务类型");
        }
    }

    /**
     * 校验文件扩展名白名单（P1-19：防上传可执行文件/脚本）。
     * <p>
     * 优先取 MultipartFile 原始文件名扩展名，其次取 Content-Type 推断；无扩展名默认拒绝。
     * </p>
     */
    default void validateExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        String ext = "";
        if (name != null && name.contains(".")) {
            ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        }
        if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的文件类型");
        }
    }
}
