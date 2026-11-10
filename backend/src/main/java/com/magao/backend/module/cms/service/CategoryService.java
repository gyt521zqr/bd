package com.magao.backend.module.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.module.cms.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> listByType(String type);
}
