package com.petsaas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.common.domain.Order;
import com.petsaas.vo.DashboardVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 获取今日统计数据
     * @param merchantId 商户ID
     * @return 统计结果
     */
    Map<String, Object> getTodayStats(@Param("merchantId") Long merchantId);
    
    /**
     * 获取热销服务Top3
     * @param merchantId 商户ID
     * @return 热销服务列表
     */
    List<DashboardVo.HotServiceVo> getHotServices(@Param("merchantId") Long merchantId);
    
    /**
     * 获取累计会员数
     * @param merchantId 商户ID
     * @return 会员数
     */
    Integer getTotalMemberCount(@Param("merchantId") Long merchantId);
}