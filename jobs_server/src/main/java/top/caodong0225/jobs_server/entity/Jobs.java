package top.caodong0225.jobs_server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *     职位表实体类
 * </p>
 * @author jyzxc
 */
@Getter
@Setter
public class Jobs implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotBlank
    private String name;
    @Pattern(message = "薪资必须是'1000~2000'格式，用~分割的格式", regexp = "^(\\d+)(~\\d+)?$")
    private String salary;
    @NotBlank
    private String description;
    private Integer userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Pattern(regexp = "^(pending|approved|rejected)$", message = "状态只能是 pending、approved 或 rejected")
    private String status;
}
