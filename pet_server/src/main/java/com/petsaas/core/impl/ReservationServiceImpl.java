package com.petsaas.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.Reservation; // 修正引入
import com.petsaas.core.entity.ServiceSlot;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.ReservationMapper; // 修正引入
import com.petsaas.core.mapper.ServiceSlotMapper;
import com.petsaas.core.mapper.UserMapper;
import com.petsaas.core.mq.ReservationProducer;
import com.petsaas.core.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired private ServiceSlotMapper slotMapper;
    @Autowired private ReservationMapper reservationMapper; // 修正为 ReservationMapper
    @Autowired private UserMapper userMapper;
    @Autowired private ReservationProducer reservationProducer; // [新增] 注入 MQ 生产者

    @Override
    public List<ServiceSlot> getAvailableSlots(String type) {
        // 注意：ServiceSlotMapper 需要有 findAvailableByType 方法，如果没有建议用 Mybatis-Plus Wrapper 写法
        // 这里假设你已经在 Mapper XML 或注解里实现了该方法
        return slotMapper.findAvailableByType(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBooking(Long userId, Long slotId, String petName, LocalDateTime appointmentTime) {
        // 1. 检查工位
        ServiceSlot slot = slotMapper.selectById(slotId);
        if (slot == null || slot.getStatus() == 0) {
            throw new RuntimeException("该工位暂时不可预约");
        }

        // 2. 检查时间冲突 (简化版：同一工位同一小时只能约一个)
        // 构造查询条件：status != 'CANCELLED' AND station_id = slotId AND start_time = appointmentTime
        // 建议使用 Wrapper 替代手写 SQL 以避免 XML 缺失问题
        Long conflictCount = reservationMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Reservation>()
                .eq("station_id", slotId) // 注意：SQL表字段是 station_id
                .eq("start_time", appointmentTime)
                .ne("status", "CANCELLED"));

        if (conflictCount > 0) {
            throw new RuntimeException("该时间段已被预约，请更换时间");
        }

        // 3. 检查余额 (假设 User 有 points 字段作为余额)
        User user = userMapper.selectById(userId);
        // 注意：这里假设 points 是 Integer，如果是 BigDecimal 请用 compareTo
        if (user.getPoints() < slot.getPrice().intValue()) {
            throw new RuntimeException("积分余额不足");
        }

        // 4. 创建预约记录 (Reservation)
        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setSeatId(slotId); // 对应实体类的 seatId (映射数据库 station_id)
        // r.setPetName(petName); // 注意：你的数据库 service_appointment 表目前没有 pet_name 字段！暂时忽略，或需要修改数据库添加
        r.setStartTime(appointmentTime);
        r.setEndTime(appointmentTime.plusHours(1)); // 默认时长1小时
        r.setStatus("RESERVED");
        r.setTotalPrice(slot.getPrice());
        r.setCreateTime(LocalDateTime.now());

        reservationMapper.insert(r);

        // 5. 扣费
        user.setPoints(user.getPoints() - slot.getPrice().intValue());
        userMapper.updateById(user);

        // 6. [核心] 发送 RabbitMQ 延迟消息 (15分钟后检查是否签到)
        reservationProducer.sendDelayMessage(r.getId());
    }

    @Override
    public List<Reservation> getMyBookings(Long userId) {
        return reservationMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Reservation>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Reservation r = reservationMapper.selectById(bookingId);
        if (r != null && !"CANCELLED".equals(r.getStatus())) {
            r.setStatus("CANCELLED");
            reservationMapper.updateById(r);
            // TODO: 退款逻辑可在此添加
        }
    }
}