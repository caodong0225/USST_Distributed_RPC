package top.caodong0225.server.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import top.caodong0225.server.dto.BaseResponseDTO;
import top.caodong0225.server.dto.GeneralDataResponseDTO;
import top.caodong0225.server.entity.UserRoles;
import top.caodong0225.server.entity.Users;
import top.caodong0225.server.service.IUserRolesService;
import top.caodong0225.server.service.IUsersService;
import top.caodong0225.server.util.JWTUtil;

import java.io.Serializable;

/**
 * @author jyzxc
 */
@RestController
@RequestMapping("/user")
public class UsersController {
    private final IUsersService usersService;
    private final IUserRolesService userRolesService;

    public UsersController(IUsersService usersService, IUserRolesService userRolesService) {
        this.usersService = usersService;
        this.userRolesService = userRolesService;
    }

    @GetMapping("/renew")
    public BaseResponseDTO renew(HttpServletRequest request) throws JOSEException {
        // 续签JWT token
        Object userId = request.getSession().getAttribute("userId");
        if (userId == null) {
            return new BaseResponseDTO(400, "Token不能为空");
        }
        UserRoles userRoles = userRolesService.getUserRolesByUserId(Integer.parseInt(userId.toString()));
        Users user = usersService.getById((Serializable) userId);
        return new GeneralDataResponseDTO<>(JWTUtil.generateToken(
                user.getId().toString(),
                user.getUsername(),
                userRoles.getRole(),
                user.getEmail()
        ));
    }

    @PostMapping("/register")
    public BaseResponseDTO register(@Valid @RequestBody Users user) {
        if(user.getEmail() == null){
            return new BaseResponseDTO(400, "邮箱不能为空");
        }
        user.setId(null);
        user.setHash(BCrypt.hashpw(user.getHash(), BCrypt.gensalt()));
        return new BaseResponseDTO(200, usersService.addUsers(user) ? "注册成功" : "注册失败");
    }

    @PostMapping("/login")
    public BaseResponseDTO login(@Valid @RequestBody Users user) throws JOSEException {
        Users dbUser = usersService.getUsersByUsername(user.getUsername());
        if (dbUser == null) {
            return new BaseResponseDTO(400, "用户不存在");
        }
        if (!BCrypt.checkpw(user.getHash(), dbUser.getHash())) {
            return new BaseResponseDTO(400, "密码错误");
        }
        UserRoles userRoles = userRolesService.getUserRolesByUserId(dbUser.getId());
        if(userRoles == null) {
            userRoles = new UserRoles();
            userRoles.setUserId(dbUser.getId());
            userRoles.setRole("guest");
        }
        return new GeneralDataResponseDTO<>(JWTUtil.generateToken(dbUser.getId().toString(),dbUser.getUsername(),userRoles.getRole(), dbUser.getEmail()));
    }

}
