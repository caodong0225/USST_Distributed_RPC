package top.caodong0225.jobs_server.vo;

import lombok.Getter;
import lombok.Setter;
import top.caodong0225.jobs_server.entity.Users;

import java.time.LocalDateTime;

/**
 * @author jyzxc
 */
@Getter
@Setter
public class JobsVO {
    // Jobs 表字段
    private Integer id;
    private String name;
    private String salary;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;

    // 嵌套的用户对象
    private Users user;
}