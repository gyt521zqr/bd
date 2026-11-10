package com.magao.backend.module.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.module.cms.entity.Partner;

import java.util.List;

public interface PartnerService extends IService<Partner> {
    List<Partner> listAll();
}
