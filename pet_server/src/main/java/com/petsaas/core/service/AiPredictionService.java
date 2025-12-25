package com.petsaas.core.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petsaas.core.entity.ServiceSlot;
import com.petsaas.core.entity.StationBusyStats;
import com.petsaas.core.mapper.StationBusyStatsMapper;
import com.petsaas.core.mapper.ServiceSlotMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiPredictionService {

    private static final Logger log = LoggerFactory.getLogger(AiPredictionService.class);

    private final ServiceSlotMapper serviceSlotMapper;
    private final StationBusyStatsMapper busyStatsMapper;

    // Python 服务地址
    private static final String AI_URL = "http://localhost:5000/predict";

    public AiPredictionService(ServiceSlotMapper serviceSlotMapper, StationBusyStatsMapper busyStatsMapper) {
        this.serviceSlotMapper = serviceSlotMapper;
        this.busyStatsMapper = busyStatsMapper;
    }

    /**
     * 每天凌晨 1 点执行，预测明天的热�?
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void syncHeatScores() {
        log.info("开始执�?AI 热度预测任务...");

        // 1. 获取所有服务工�?ID
        List<ServiceSlot> slots = serviceSlotMapper.selectList(null);
        if (slots.isEmpty()) return;

        List<Long> slotIds = slots.stream().map(ServiceSlot::getId).collect(Collectors.toList());

        // 2. 调用 Python 接口
        Map<String, Object> param = new HashMap<>();
        param.put("slotIds", slotIds);

        try {
            String resultJson = HttpUtil.post(AI_URL, JSONUtil.toJsonStr(param));
            JSONObject result = JSONUtil.parseObj(resultJson);

            if (result.getInt("code") == 200) {
                JSONArray data = result.getJSONArray("data");

                // 3. 解析结果并入�?
                for (Object item : data) {
                    JSONObject obj = (JSONObject) item;
                    Long slotId = obj.getLong("slotId");
                    Double score = obj.getDouble("busyScore");

                    // 构造实�?
                    StationBusyStats stats = new StationBusyStats();
                    stats.setStationId(slotId);
                    stats.setBusyScore(score);
                    stats.setPredictionDate(LocalDate.now().plusDays(1)); // 预测明天
                    stats.setUpdateTime(LocalDateTime.now());

                    // 先删后插 (简�?Upsert)
                    QueryWrapper<StationBusyStats> deleteQuery = new QueryWrapper<>();
                    deleteQuery.eq("station_id", slotId).eq("prediction_date", stats.getPredictionDate());
                    busyStatsMapper.delete(deleteQuery);

                    busyStatsMapper.insert(stats);
                }
                log.info("AI 工位繁忙度数据同步完成，共更�?{} �?, data.size());
            }
        } catch (Exception e) {
            log.error("调用 AI 服务失败 (请检�?Python 脚本是否运行): {}", e.getMessage());
        }
    }
}