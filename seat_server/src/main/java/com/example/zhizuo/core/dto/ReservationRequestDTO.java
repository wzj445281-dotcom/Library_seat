package com.example.zhizuo.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(description = "预约请求对象")
public class ReservationRequestDTO {

    @Schema(description = "用户ID", hidden = true) // 后端自动从Token获取，或前端传
    private Long userId;

    @Schema(description = "座位ID", example = "1")
    @NotNull(message = "座位ID不能为空")
    private Long seatId;

    @Schema(description = "开始时间", example = "2023-12-15 14:00:00")
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 自动将字符串转为 LocalDateTime
    @Future(message = "预约时间必须是将来")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2023-12-15 16:00:00")
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}