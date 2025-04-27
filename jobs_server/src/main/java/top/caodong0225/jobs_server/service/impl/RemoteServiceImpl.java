package top.caodong0225.jobs_server.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import top.caodong0225.jobs_server.dto.BaseDataResponseDTO;
import top.caodong0225.jobs_server.dto.BaseResponseDTO;
import top.caodong0225.jobs_server.dto.GeneralDataResponseDTO;
import top.caodong0225.jobs_server.dto.UserRolesResponseDTO;
import top.caodong0225.jobs_server.entity.Users;
import top.caodong0225.jobs_server.service.IRemoteService;

/**
 * @author jyzxc
 */
@Service
public class RemoteServiceImpl {
    private final IRemoteService remoteServiceClient;

    // 构造方法注入
    public RemoteServiceImpl(IRemoteService remoteServiceClient) {
        this.remoteServiceClient = remoteServiceClient;
    }

    public GeneralDataResponseDTO<UserRolesResponseDTO> getUser(Long id) {
        return remoteServiceClient.getUserById(id);
    }

    public BaseDataResponseDTO login(Users users){
        return remoteServiceClient.login(users);
    }

    public BaseResponseDTO register(Users users){
        return remoteServiceClient.register(users);
    }

}
