package com.petsaas.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVo implements Serializable {
    private BigDecimal todayIncome;      // 今日营收
    private Integer todayOrderCount;     // 今日订单数
    private Integer totalMemberCount;    // 累计会员(可选)
    private List<HotServiceVo> hotServices; // 热销服务列表

    @Data
    public static class HotServiceVo implements Serializable {
        private String name;
        private Integer count;
    }
}