package top.caodong0225.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.caodong0225.server.entity.UserRoles;

public interface IUserRolesService extends IService<UserRoles> {
    UserRoles getUserRolesByUserId(Integer userId);
}
