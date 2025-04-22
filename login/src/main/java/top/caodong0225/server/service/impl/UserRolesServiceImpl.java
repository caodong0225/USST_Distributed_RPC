package top.caodong0225.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.caodong0225.server.entity.UserRoles;
import top.caodong0225.server.mapper.UserRolesMapper;
import top.caodong0225.server.service.IUserRolesService;

/**
 * @author jyzxc
 */
@Service
public class UserRolesServiceImpl extends ServiceImpl<UserRolesMapper, UserRoles> implements IUserRolesService {
    @Override
    public UserRoles getUserRolesByUserId(Integer userId) {
        return this.lambdaQuery()
                .eq(UserRoles::getUserId, userId)
                .one();
    }
}
