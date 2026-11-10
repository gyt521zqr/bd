package com.magao.backend.module.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.module.cms.entity.Banner;

import java.util.List;

public interface BannerService extends IService<Banner> {
    List<Banner> listByPosition(String position);
}
