package top.caodong0225.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.caodong0225.server.entity.Users;
import top.caodong0225.server.mapper.UsersMapper;
import top.caodong0225.server.service.IUsersService;

/**
 * @author jyzxc
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService  {

    @Override
    public boolean addUsers(Users users) {
        return this.save(users);
    }

    @Override
    public Users getUsersByUsername(String username) {
        return this.getOne(new QueryWrapper<Users>().eq("username", username));
    }
}
