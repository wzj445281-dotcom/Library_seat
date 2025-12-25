import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceSlot {
    private Long id;

    // 原本是 label/seatNumber
    private String name;  // 例如："洗护位-01"

    // 原本是 grid_x/y，现在改为类型
    private String type;  // 枚举值：BATH, GROOM, MEDICAL

    private Integer status; // 1=可用, 0=维护中

    // 新增：基础服务费
    private BigDecimal basePrice;

    private String description;
}
