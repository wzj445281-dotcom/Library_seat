package com.petsaas.core.entity;

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
  private Integer status; // 0=未处�? 1=已处�?
  private LocalDateTime createTime;
}