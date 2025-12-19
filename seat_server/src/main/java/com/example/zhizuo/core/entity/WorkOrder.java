package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("work_orders")
public class WorkOrder implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;

    private Long userId;

    /**
     * NOISE, REPAIR, CLEAN
     */
    private String category;

    private String content;

    private String snapshotImg;

    /**
     * AI判定优先级: 0低 1中 2高
     */
    private Integer priority;

    private String aiAnalysisResult;

    /**
     * PENDING, PROCESSING, RESOLVED
     */
    private String status;

    private Long handlerId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}