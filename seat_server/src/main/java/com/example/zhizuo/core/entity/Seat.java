package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer gridX;
    private Integer gridY;
    private String label;
    private Integer status; // 1-可用 2-已占用 3-维修中
    private Double heatScore; // 热度值

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getGridX() { return gridX; }
    public void setGridX(Integer gridX) { this.gridX = gridX; }
    public Integer getGridY() { return gridY; }
    public void setGridY(Integer gridY) { this.gridY = gridY; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Double getHeatScore() { return heatScore; }
    public void setHeatScore(Double heatScore) { this.heatScore = heatScore; }
    public void setHeatScore(double heatScore) { this.heatScore = heatScore; }
}