package com.magao.backend.module.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.module.cms.entity.Partner;
import com.magao.backend.module.cms.mapper.PartnerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl extends ServiceImpl<PartnerMapper, Partner> implements PartnerService {

    @Override
    public List<Partner> listAll() {
        LambdaQueryWrapper<Partner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Partner::getStatus, 1)
               .orderByAsc(Partner::getSortOrder)
               .orderByAsc(Partner::getId);
        return list(wrapper);
    }
}
