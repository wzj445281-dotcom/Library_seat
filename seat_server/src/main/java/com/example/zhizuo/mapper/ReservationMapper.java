package com.example.zhizuo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
    // 核心冲突检测 SQL
    @Select("SELECT COUNT(*) FROM reservation " +
            "WHERE seat_id=#{seatId} " +
            "AND status IN ('RESERVED','CHECKED_IN') " +
            "AND NOT (end_time <= #{start} OR start_time >= #{end})")
    int countConflict(@Param("seatId") Long seatId,
                      @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end);
}