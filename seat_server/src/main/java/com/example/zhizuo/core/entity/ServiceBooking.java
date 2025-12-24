package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("service_bookings")
public class ServiceBooking {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String bookingNo; // 预约单号
    private Long userId; // 用户ID
    private Long slotId; // 工位ID
    private String petName; // 宠物昵称
    private String petType; // 宠物种类 (Dog/Cat)
    private LocalDateTime appointmentTime; // 预约时间
    private Integer durationMinutes; // 预计耗时(分钟)
    private String status; // 状态: PENDING, CONFIRMED, COMPLETED, CANCELLED
    private Double totalPrice; // 服务费用
    private LocalDateTime createTime; // 创建时间
    
    // 手动添加getter和setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingNo() { return bookingNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getPetType() { return petType; }
    public void setPetType(String petType) { this.petType = petType; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}