package com.magao.backend.module.cms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.cms.entity.News;

public interface NewsService extends IService<News> {
    Page<News> pageList(PageDTO pageDTO, Long categoryId, Integer status);
    News getDetail(Long id);
    void incrementViewCount(Long id);
}
