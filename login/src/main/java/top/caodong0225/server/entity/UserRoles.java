package top.caodong0225.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author jyzxc
 */
@Getter
@Setter
public class UserRoles implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotBlank
    private Integer userId;
    @Pattern(regexp = "^(admin|super-admin)$", message = "角色只能是 admin 或 super-admin")
    private String role;
}
