package main.java.com.example.zhizuo.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("seats") // 对应数据库表名
public class Seat {
    private Long id;
    private String label;
    private Integer gridX;
    private Integer gridY;
    private Integer status; // 1可用 0维修
}