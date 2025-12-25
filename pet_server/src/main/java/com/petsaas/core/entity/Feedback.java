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
  private String type;
  private String contact;
  private Integer status; // 0=未处理 1=已处理
  private LocalDateTime createTime;
  
  // 显式添加getter/setter方法
  public Long getId() {
      return id;
  }
  
  public void setId(Long id) {
      this.id = id;
  }
  
  public Long getUserId() {
      return userId;
  }
  
  public void setUserId(Long userId) {
      this.userId = userId;
  }
  
  public String getContent() {
      return content;
  }
  
  public void setContent(String content) {
      this.content = content;
  }
  
  public String getType() {
      return type;
  }
  
  public void setType(String type) {
      this.type = type;
  }
  
  public String getContact() {
      return contact;
  }
  
  public void setContact(String contact) {
      this.contact = contact;
  }
  
  public Integer getStatus() {
      return status;
  }
  
  public void setStatus(Integer status) {
      this.status = status;
  }
  
  public LocalDateTime getCreateTime() {
      return createTime;
  }
  
  public void setCreateTime(LocalDateTime createTime) {
      this.createTime = createTime;
  }
}