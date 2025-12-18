package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private String content;
  private String contact;
  private Integer status; // 0=未处理, 1=已处理
  private LocalDateTime createTime;
}