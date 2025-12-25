package com.petsaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.core.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    // 关键 SQL：检测时间重�?    // 逻辑�?A.start < B.end) AND (A.end > B.start) 即为重叠
    @Select("SELECT COUNT(*) FROM reservation " +
            "WHERE seat_id = #{seatId} " +
            "AND status IN ('RESERVED', 'CHECKED_IN') " +
            "AND start_time < #{end} AND end_time > #{start}")
    int countConflict(@Param("seatId") Long seatId,
                      @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end);
}