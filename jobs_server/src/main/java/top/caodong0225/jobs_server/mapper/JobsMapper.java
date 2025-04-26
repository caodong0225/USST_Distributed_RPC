package top.caodong0225.jobs_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.caodong0225.jobs_server.entity.Jobs;
import top.caodong0225.jobs_server.vo.JobsVO;

import java.util.List;

/**
 * @author jyzxc
 */
@Mapper
public interface JobsMapper extends BaseMapper<Jobs> {
    List<JobsVO> getJobsList(
            @Param("userId") Integer userId,
            @Param("status") String status,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("name") String name,
            @Param("salaryDown") String salaryDown,
            @Param("salaryUp") String salaryUp
    );
}
