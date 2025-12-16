package com.example.zhizuo.core.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.mapper.SeatMapper;
import com.example.zhizuo.core.service.AdminSeatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminSeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements AdminSeatService {

    // 继承 ServiceImpl 后，baseMapper 就是 SeatMapper，可以直接用

    @Override
    @Transactional(rollbackFor = Exception.class) // 确保批量操作要么全成功，要么全失败
    public void batchCreateSeats(int row, int count) {
        // 模拟一点复杂的校验逻辑，增加代码量
        if (row < 0 || count <= 0) {
            throw new IllegalArgumentException("排号或数量不合法");
        }

        // 这里的逻辑和之前一样，但搬到了 Service 层
        for (int i = 1; i <= count; i++) {
            Seat seat = new Seat();
            seat.setGridX(i);
            seat.setGridY(row);
            seat.setLabel(row + "排" + i + "号");
            seat.setStatus(1); // 默认可用
            // 使用 ServiceImpl 提供的 save 方法
            this.save(seat);
        }
    }

    @Override
    public List<Seat> getAllSeats() {
        // 可以在这里加缓存逻辑 (Redis)，以后升级用
        return this.list();
    }
}