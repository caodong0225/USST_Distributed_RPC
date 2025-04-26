package top.caodong0225.server.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserRolesResponseDTO {
    private Integer id;
    private String role;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
