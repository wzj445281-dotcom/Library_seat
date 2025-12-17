package com.example.zhizuo.core.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.entity.SeatHeatStats;
import com.example.zhizuo.core.mapper.SeatHeatStatsMapper;
import com.example.zhizuo.core.mapper.SeatMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiPredictionService {

    private final SeatMapper seatMapper;
    private final SeatHeatStatsMapper heatStatsMapper;

    // Python 服务地址
    private static final String AI_URL = "http://localhost:5000/predict";

    public AiPredictionService(SeatMapper seatMapper, SeatHeatStatsMapper heatStatsMapper) {
        this.seatMapper = seatMapper;
        this.heatStatsMapper = heatStatsMapper;
    }

    /**
     * 每天凌晨 1 点执行，预测明天的热度
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void syncHeatScores() {
        log.info("开始执行 AI 热度预测任务...");

        // 1. 获取所有座位 ID
        List<Seat> seats = seatMapper.selectList(null);
        if (seats.isEmpty()) return;

        List<Long> seatIds = seats.stream().map(Seat::getId).collect(Collectors.toList());

        // 2. 调用 Python 接口
        Map<String, Object> param = new HashMap<>();
        param.put("seatIds", seatIds);

        try {
            String resultJson = HttpUtil.post(AI_URL, JSONUtil.toJsonStr(param));
            JSONObject result = JSONUtil.parseObj(resultJson);

            if (result.getInt("code") == 200) {
                JSONArray data = result.getJSONArray("data");

                // 3. 解析结果并入库
                for (Object item : data) {
                    JSONObject obj = (JSONObject) item;
                    Long seatId = obj.getLong("seatId");
                    Double score = obj.getDouble("heatScore");

                    // 构造实体
                    SeatHeatStats stats = new SeatHeatStats();
                    stats.setSeatId(seatId);
                    stats.setHeatScore(score);
                    stats.setPredictionDate(LocalDate.now().plusDays(1)); // 预测明天
                    stats.setUpdateTime(LocalDateTime.now());

                    // 先删后插 (简单 Upsert)
                    QueryWrapper<SeatHeatStats> deleteQuery = new QueryWrapper<>();
                    deleteQuery.eq("seat_id", seatId).eq("prediction_date", stats.getPredictionDate());
                    heatStatsMapper.delete(deleteQuery);

                    heatStatsMapper.insert(stats);
                }
                log.info("AI 热度数据同步完成，共更新 {} 条", data.size());
            }
        } catch (Exception e) {
            log.error("调用 AI 服务失败 (请检查 Python 脚本是否运行): {}", e.getMessage());
        }
    }
}