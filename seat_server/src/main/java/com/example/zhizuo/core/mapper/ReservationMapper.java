package com.example.zhizuo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.core.entity.ServiceBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;

@Mapper
public interface ReservationMapper extends BaseMapper<ServiceBooking> {

    // 关键 SQL：检测时间重叠
    // 逻辑：(A.start < B.end) AND (A.end > B.start) 即为重叠
    // 注意：表名和字段名需要根据实际数据库表结构调整
    // ServiceBooking实体使用slotId字段，对应数据库可能是slot_id或seat_id
    // appointmentTime对应数据库可能是appointment_time
    @Select("SELECT COUNT(*) FROM service_booking " +
            "WHERE slot_id = #{seatId} " +
            "AND status IN ('RESERVED', 'CHECKED_IN') " +
            "AND appointment_time < #{end} AND DATE_ADD(appointment_time, INTERVAL duration_minutes MINUTE) > #{start}")
    int countConflict(@Param("seatId") Long seatId,
                      @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end);
}