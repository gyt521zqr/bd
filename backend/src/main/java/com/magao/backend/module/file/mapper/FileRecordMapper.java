package com.magao.backend.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.file.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件记录Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
}
