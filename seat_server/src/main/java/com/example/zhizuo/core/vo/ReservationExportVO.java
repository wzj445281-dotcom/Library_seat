package com.example.zhizuo.core.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationExportVO {

    @ExcelProperty("订单ID")
    @ColumnWidth(15)
    private Long id;

    @ExcelProperty("学号")
    @ColumnWidth(20)
    private String studentId;

    @ExcelProperty("姓名")
    @ColumnWidth(15)
    private String userName;

    @ExcelProperty("座位号")
    @ColumnWidth(15)
    private String seatLabel;

    @ExcelProperty("开始时间")
    @ColumnWidth(25)
    private LocalDateTime startTime;

    @ExcelProperty("结束时间")
    @ColumnWidth(25)
    private LocalDateTime endTime;

    @ExcelProperty("状态")
    @ColumnWidth(15)
    private String status;

    @ExcelProperty("创建时间")
    @ColumnWidth(25)
    private LocalDateTime createTime;
}