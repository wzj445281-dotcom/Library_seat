package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("credit_logs")
public class CreditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type; // ADD æˆ?REDUCE
    private Integer score;
    private String reason;
    private LocalDateTime createTime;
}