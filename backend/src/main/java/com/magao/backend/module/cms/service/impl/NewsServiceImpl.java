package com.magao.backend.module.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.cms.entity.News;
import com.magao.backend.module.cms.mapper.NewsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    @Override
    public Page<News> pageList(PageDTO pageDTO, Long categoryId, Integer status) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(News::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(News::getStatus, status);
        }
        wrapper.orderByDesc(News::getIsPinned)
               .orderByDesc(News::getSortOrder)
               .orderByDesc(News::getPublishTime);
        return page(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), wrapper);
    }

    @Override
    public News getDetail(Long id) {
        return getById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        News news = getById(id);
        if (news != null) {
            news.setViewCount((news.getViewCount() == null ? 0 : news.getViewCount()) + 1);
            updateById(news);
        }
    }
}
