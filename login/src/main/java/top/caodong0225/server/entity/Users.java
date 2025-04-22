package top.caodong0225.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author jyzxc
 */
@Getter
@Setter
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotNull
    @NotBlank
    private String username;
    @NotNull
    @NotBlank
    private String hash;
    private String email;
}
