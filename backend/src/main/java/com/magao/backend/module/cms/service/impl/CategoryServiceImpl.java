package com.magao.backend.module.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.module.cms.entity.Category;
import com.magao.backend.module.cms.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> listByType(String type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Category::getType, type);
        }
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getId);
        return list(wrapper);
    }
}
