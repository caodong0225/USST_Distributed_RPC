package top.caodong0225.jobs_server.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author jyzxc
 */
@Getter
@Setter
public class UserRolesResponseDTO {
    private Integer id;
    private String role;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
