package com.magao.backend.module.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.module.cms.entity.Banner;
import com.magao.backend.module.cms.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<Banner> listByPosition(String position) {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        if (position != null && !position.isEmpty()) {
            wrapper.eq(Banner::getPosition, position);
        }
        wrapper.eq(Banner::getStatus, 1)
               .orderByAsc(Banner::getSortOrder)
               .orderByAsc(Banner::getId);
        return list(wrapper);
    }
}
