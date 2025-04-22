package top.caodong0225.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.caodong0225.server.entity.Users;

public interface IUsersService extends IService<Users> {
    boolean addUsers(Users users);
    Users getUsersByUsername(String username);
}
