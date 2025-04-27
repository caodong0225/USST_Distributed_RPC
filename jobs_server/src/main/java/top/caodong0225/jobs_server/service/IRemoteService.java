package top.caodong0225.jobs_server.service;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import top.caodong0225.jobs_server.dto.BaseDataResponseDTO;
import top.caodong0225.jobs_server.dto.BaseResponseDTO;
import top.caodong0225.jobs_server.dto.GeneralDataResponseDTO;
import top.caodong0225.jobs_server.dto.UserRolesResponseDTO;
import top.caodong0225.jobs_server.entity.Users;

/**
 * @author jyzxc
 */
public interface IRemoteService {
    @RequestLine("GET /call/login/user/{id}")
    GeneralDataResponseDTO<UserRolesResponseDTO> getUserById(@Param("id") Long id);

    @RequestLine("POST /call/login/user/login")
    @Headers("Content-Type: application/json")
    BaseDataResponseDTO login(Users users);

    @RequestLine("POST /call/login/user/register")
    @Headers("Content-Type: application/json")
    BaseResponseDTO register(Users users);

}
