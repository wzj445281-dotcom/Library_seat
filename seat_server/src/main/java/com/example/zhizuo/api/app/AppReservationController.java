// ... existing imports ...
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.service.ReservationService;
import com.example.zhizuo.core.mapper.ReservationMapper; // 临时为了方便直接查
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/app/reservation")
public class AppReservationController {

    private final ReservationService service;
    private final ReservationMapper reservationMapper; // 注入Mapper方便查询

    public AppReservationController(ReservationService service, ReservationMapper reservationMapper) {
        this.service = service;
        this.reservationMapper = reservationMapper;
    }

    // 1. 获取我的预约列表 (核心新增)
    @GetMapping("/list")
    public ApiResponse<List<Reservation>> getMyList() {
        // 从 Spring Security 上下文获取当前登录用户的学号
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentId = (String) auth.getPrincipal(); // 这里的 Principal 是学号

        // 先查用户ID (实际项目中可以在 UserDetails 里直接存 ID，这里多查一次)
        // ... 为了代码简洁，这里假设前端传了 userId 或者我们之前存了 Session，
        // 但最安全的是根据 Token 里的 studentId 反查 user_id。
        // 这里简化逻辑：我们假设 Token 解析逻辑里把 userId 放进去了，或者直接前端传 userId (虽然不安全但快)
        // 为了演示规范，我们用最简单的：按创建时间倒序查所有

        // ⚠️ 注意：为了严谨，这里应该根据 studentId 查 userId。
        // 但我们在 AuthController 登录时返回了 userId 给前端，前端可以传过来，或者我们这里只演示查询逻辑

        // 修正：我们直接让前端传 userId 过来查询，或者完全依赖 Token。
        // 这里演示依赖 Token 解析出的 studentId (用户名)
        return ApiResponse.success(service.getUserReservations(studentId));
    }

    // 2. 签到接口
    @PostMapping("/checkin/{id}")
    public ApiResponse<String> checkIn(@PathVariable Long id) {
        try {
            service.checkIn(id);
            return ApiResponse.success("签到成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 3. 取消接口
    @PostMapping("/cancel/{id}")
    public ApiResponse<String> cancel(@PathVariable Long id) {
        try {
            service.cancel(id);
            return ApiResponse.success("取消成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // ... reserve 接口保持不变 ...
}