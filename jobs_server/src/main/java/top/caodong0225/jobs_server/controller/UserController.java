package top.caodong0225.jobs_server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import top.caodong0225.jobs_server.dto.BaseDataResponseDTO;
import top.caodong0225.jobs_server.dto.BaseResponseDTO;
import top.caodong0225.jobs_server.dto.GeneralDataResponseDTO;
import top.caodong0225.jobs_server.dto.UserRolesResponseDTO;
import top.caodong0225.jobs_server.entity.Users;
import top.caodong0225.jobs_server.service.IRemoteService;

/**
 * @author jyzxc
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final IRemoteService remoteService;

    public UserController(IRemoteService remoteService) {
        this.remoteService = remoteService;
    }

    @GetMapping("/{id}")
    public GeneralDataResponseDTO<UserRolesResponseDTO> getUserById(@PathVariable("id") Long id) {
        return remoteService.getUserById(id);
    }

    @PostMapping("/login")
    public BaseDataResponseDTO login(@RequestBody Users user) {
        return remoteService.login(user);
    }

    @PostMapping("/register")
    public BaseResponseDTO register(@RequestBody Users user) {
        return remoteService.register(user);
    }

}
