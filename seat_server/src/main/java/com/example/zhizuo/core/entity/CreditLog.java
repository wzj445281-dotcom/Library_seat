package com.example.zhizuo.core.entity;

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
    private String type; // ADD 或 REDUCE
    private Integer score;
    private String reason;
    private LocalDateTime createTime;
}