package com.petsaas.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petsaas.common.domain.ServiceSchedule;
import com.petsaas.dto.BatchPublishReq;

public interface ServiceScheduleService extends IService<ServiceSchedule> {
    
    /**
     * 批量发布排期
     * @param merchantId 商户ID
     * @param req 批量发布请求
     */
    void batchPublish(Long merchantId, BatchPublishReq req);
}