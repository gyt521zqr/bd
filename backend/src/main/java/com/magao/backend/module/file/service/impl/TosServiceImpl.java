package com.magao.backend.module.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.util.TosUtil;
import com.magao.backend.module.file.entity.FileRecord;
import com.magao.backend.module.file.mapper.FileRecordMapper;
import com.magao.backend.module.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务实现类（基于火山引擎 TOS）
 * <p>
 * 在 TOS 启用后替代 MinIO 实现，通过 @ConditionalOnProperty 控制激活。
 * 当 tos.enabled=true 时，MinioServiceImpl 不会被加载。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "tos.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class TosServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    private final TosUtil tosUtil;

    @Override
    public FileRecord upload(MultipartFile file, String bizType, Long bizId) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "上传文件不能为空");
        }

        validateBizType(bizType);   // P1-19：防路径穿越
        validateExtension(file);    // P1-19：防上传可执行文件

        String folder = bizType + "/";
        String objectKey = tosUtil.upload(file, folder);

        FileRecord record = new FileRecord();
        record.setOriginalName(file.getOriginalFilename());
        record.setFileName(objectKey.substring(objectKey.lastIndexOf("/") + 1));
        record.setFilePath(objectKey);
        record.setFileSize(file.getSize());
        record.setFileType(file.getContentType());
        String originalName = file.getOriginalFilename();
        record.setExtension(originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1) : "");
        record.setBucket("magao");
        record.setBizType(bizType);
        record.setBizId(bizId);

        save(record);
        log.info("TOS 文件上传成功：fileId={}, key={}", record.getId(), objectKey);
        return record;
    }

    @Override
    public String getDownloadUrl(Long fileId) {
        FileRecord record = getById(fileId);
        if (record == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return tosUtil.getPresignedUrl(record.getFilePath(), record.getBucket(), 3600);
    }

    @Override
    public InputStream download(Long fileId) {
        FileRecord record = getById(fileId);
        if (record == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return tosUtil.download(record.getFilePath(), record.getBucket());
    }

    @Override
    public void deleteFile(Long fileId) {
        FileRecord record = getById(fileId);
        if (record == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        tosUtil.delete(record.getFilePath(), record.getBucket());
        removeById(fileId);
        log.info("TOS 文件删除成功：fileId={}", fileId);
    }
}