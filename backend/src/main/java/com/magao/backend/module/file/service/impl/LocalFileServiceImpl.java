package com.magao.backend.module.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.file.entity.FileRecord;
import com.magao.backend.module.file.mapper.FileRecordMapper;
import com.magao.backend.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnMissingBean({MinioServiceImpl.class, TosServiceImpl.class})
public class LocalFileServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    private static final String UPLOAD_DIR = "C:\\tools\\magao-build\\uploads";

    @Override
    public FileRecord upload(MultipartFile file, String bizType, Long bizId) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "上传文件不能为空");
        }
        try {
            String folder = bizType + "/";
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf(".") + 1) : "";
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path dirPath = Paths.get(UPLOAD_DIR, folder);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(fileName);
            Files.write(filePath, file.getBytes());
            FileRecord record = new FileRecord();
            record.setOriginalName(originalName);
            record.setFileName(fileName);
            record.setFilePath(folder + fileName);
            record.setFileSize(file.getSize());
            record.setFileType(file.getContentType());
            record.setExtension(extension);
            record.setBucket("local");
            record.setBizType(bizType);
            record.setBizId(bizId);
            save(record);
            log.info("文件上传成功(本地)：fileId={}, fileName={}", record.getId(), originalName);
            return record;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public String getDownloadUrl(Long fileId) {
        FileRecord record = getById(fileId);
        if (record == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return "/api/file/public/download/" + fileId;
    }

    @Override
    public void deleteFile(Long fileId) {
        FileRecord record = getById(fileId);
        if (record == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        try {
            Path filePath = Paths.get(UPLOAD_DIR, record.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除本地文件失败：{}", record.getFilePath());
        }
        removeById(fileId);
        log.info("文件删除成功(本地)：fileId={}", fileId);
    }
}
