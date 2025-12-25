package com.petsaas.core.entity; 
 
import com.baomidou.mybatisplus.annotation.IdType; 
import com.baomidou.mybatisplus.annotation.TableId; 
import com.baomidou.mybatisplus.annotation.TableName; 
import lombok.Data; 
import java.time.LocalDateTime; 
 
/** 
 * 评价/反馈实体 
 * 对应数据�?reviews �?
 */ 
@Data 
@TableName("reviews") 
public class Review { 
    @TableId(type = IdType.AUTO) 
    private Long id; 
 
    private Long userId; 
    private Long relatedId; // 关联ID (商品ID 或工位ID) 
    private String type;    // PRODUCT / SERVICE 
    private Integer rating; // 1-5 
    private String content; // 内容 
    
    private LocalDateTime createTime;
    
    // 显式添加getId方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}