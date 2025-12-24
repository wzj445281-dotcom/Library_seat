package com.example.zhizuo.api.admin; 
 
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; 
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; 
import com.example.zhizuo.common.ApiResponse; 
import com.example.zhizuo.core.entity.ServiceBooking; 
import com.example.zhizuo.core.service.ReservationService; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.*; 
 
/** 
 * 后台预约订单管理 
 */ 
@RestController 
@RequestMapping("/api/admin/bookings") 
public class AdminBookingController { 
 
    @Autowired 
    private ReservationService reservationService; 
 
    // 分页查询预约列表 
    @GetMapping("/list") 
    public ApiResponse list(@RequestParam(defaultValue = "1") Integer page, 
                            @RequestParam(defaultValue = "10") Integer size, 
                            @RequestParam(required = false) String status) { 
        Page<ServiceBooking> bookingPage = new Page<>(page, size); 
        QueryWrapper<ServiceBooking> query = new QueryWrapper<>(); 
        
        if (status != null && !status.isEmpty()) { 
            query.eq("status", status); 
        } 
        query.orderByDesc("appointment_time"); 
        
        return ApiResponse.success(reservationService.page(bookingPage, query)); 
    } 
 
    // 管理员取消预约 
    @PostMapping("/cancel") 
    public ApiResponse cancel(@RequestBody ServiceBooking booking) { 
        reservationService.cancelBooking(booking.getId()); 
        return ApiResponse.success("预约已取消"); 
    } 
 
    // 标记为完成 (线下服务完成后调用) 
    @PostMapping("/complete") 
    public ApiResponse complete(@RequestBody ServiceBooking booking) { 
        ServiceBooking update = new ServiceBooking(); 
        update.setId(booking.getId()); 
        update.setStatus("COMPLETED"); 
        reservationService.updateById(update); 
        return ApiResponse.success("订单已完成"); 
    } 
}