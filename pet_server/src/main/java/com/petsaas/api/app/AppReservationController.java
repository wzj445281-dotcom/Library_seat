package com.petsaas.api.app; 
 
import com.petsaas.common.ApiResponse; 
import com.petsaas.common.util.SecurityUtils; 
import com.petsaas.core.service.ReservationService; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.*; 
 
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter; 
import java.util.Map; 
 
@RestController 
@RequestMapping("/api/app/reservation") 
public class AppReservationController { 
 
    @Autowired 
    private ReservationService reservationService; 
 
    // 获取可用工位 (BATH, GROOM, MEDICAL) 
    @GetMapping("/slots") 
    public ApiResponse getSlots(@RequestParam String type) { 
        return ApiResponse.success(reservationService.getAvailableSlots(type)); 
    } 
 
    // 提交预约 
    @PostMapping("/book") 
    public ApiResponse book(@RequestBody Map<String, Object> payload) { 
        try { 
            Long userId = SecurityUtils.getCurrentUserId(); 
            Long slotId = Long.valueOf(payload.get("slotId").toString()); 
            String petName = (String) payload.get("petName"); 
            String timeStr = (String) payload.get("appointmentTime"); // 格式: "2023-10-01 14:00:00" 
             
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
            LocalDateTime appointmentTime = LocalDateTime.parse(timeStr, formatter); 
 
            reservationService.createBooking(userId, slotId, petName, appointmentTime); 
            return ApiResponse.success("预约成功"); 
        } catch (Exception e) { 
            return ApiResponse.error(e.getMessage()); 
        } 
    } 
 
    // 我的预约 
    @GetMapping("/my") 
    public ApiResponse myBookings() { 
        Long userId = SecurityUtils.getCurrentUserId(); 
        return ApiResponse.success(reservationService.getMyBookings(userId)); 
    } 
     
    // 取消预约 
    @PostMapping("/cancel") 
    public ApiResponse cancel(@RequestBody Map<String, Long> payload) { 
        reservationService.cancelBooking(payload.get("id")); 
        return ApiResponse.success("取消成功"); 
    } 
}