// ... existing code ...
import com.example.zhizuo.service.ReservationService;
import com.example.zhizuo.common.ApiResponse; // 导入统一响应
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping("/reserve")
    public ApiResponse<String> reserve(@RequestParam Long userId,
                                       @RequestParam Long seatId,
                                       @RequestParam String start,
                                       @RequestParam String end) {
        // 使用 try-catch 的逻辑被 GlobalExceptionHandler 接管了，这里直接调用即可
        service.reserve(userId, seatId,
                LocalDateTime.parse(start),
                LocalDateTime.parse(end));
        return ApiResponse.success("预约成功");
    }

    @PostMapping("/checkin/{id}")
    public ApiResponse<String> checkIn(@PathVariable Long id) {
        service.checkIn(id);
        return ApiResponse.success("签到成功");
    }

    @PostMapping("/leave/{id}")
    public ApiResponse<String> leave(@PathVariable Long id) {
        service.leave(id);
        return ApiResponse.success("离座成功");
    }
}