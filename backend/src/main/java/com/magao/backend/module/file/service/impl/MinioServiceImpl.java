package com.magao.backend.module.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.util.MinioUtil;
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
 * 文件服务实现类（基于MinIO）
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class MinioServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    private final MinioUtil minioUtil;

    @Override
    public FileRecord upload(MultipartFile file, String bizType, Long bizId) {
        if (file.isEmpty()) { throw new BusinessException(ErrorCode.PARAM_ERROR, "上传文件不能为空"); }
        validateBizType(bizType);   // P1-19：防路径穿越
        validateExtension(file);    // P1-19：防上传可执行文件
        String folder = bizType + "/";
        String objectName = minioUtil.upload(file, folder);
        FileRecord record = new FileRecord();
        record.setOriginalName(file.getOriginalFilename());
        record.setFileName(objectName.substring(objectName.lastIndexOf("/") + 1));
        record.setFilePath(objectName);
        record.setFileSize(file.getSize());
        record.setFileType(file.getContentType());
        String originalName = file.getOriginalFilename();
        record.setExtension(originalName != null && originalName.contains(".") ?
                originalName.substring(originalName.lastIndexOf(".") + 1) : "");
        record.setBucket("magao");
        record.setBizType(bizType);
        record.setBizId(bizId);
        save(record);
        log.info("文件上传成功：fileId={}, fileName={}", record.getId(), record.getOriginalName());
        return record;
    }

    @Override
    public String getDownloadUrl(Long fileId) {
        FileRecord record = getById(fileId);
        if (record == null) { throw new BusinessException(ErrorCode.FILE_NOT_FOUND); }
        return minioUtil.getPresignedUrl(record.getFilePath(), record.getBucket(), 3600);
    }

    @Override
    public void deleteFile(Long fileId) {
        FileRecord record = getById(fileId);
        if (record == null) { throw new BusinessException(ErrorCode.FILE_NOT_FOUND); }
        minioUtil.delete(record.getFilePath(), record.getBucket());
        removeById(fileId);
        log.info("文件删除成功：fileId={}", fileId);
    }

    @Override
    public InputStream download(Long fileId) {
        FileRecord record = getById(fileId);
        if (record == null) { throw new BusinessException(ErrorCode.FILE_NOT_FOUND); }
        return minioUtil.download(record.getFilePath(), record.getBucket());
    }
}
