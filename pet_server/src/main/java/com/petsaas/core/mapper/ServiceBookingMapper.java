package com.petsaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.core.entity.ServiceBooking;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ServiceBookingMapper extends BaseMapper<ServiceBooking> {

    // 检查时间冲突：查询指定工位在指定时间小时内是否有已确认的预约
    @Select("SELECT COUNT(*) FROM sys_service_booking " +
            "WHERE slot_id = #{slotId} " +
            "AND appointment_time = #{timeStr} " +
            "AND status IN ('CONFIRMED', 'PENDING')")
    int checkConflict(@Param("slotId") Long slotId, @Param("timeStr") String timeStr);

    @Select("SELECT * FROM sys_service_booking WHERE user_id = #{userId} ORDER BY appointment_time DESC")
    List<ServiceBooking> findByUserId(@Param("userId") Long userId);
}