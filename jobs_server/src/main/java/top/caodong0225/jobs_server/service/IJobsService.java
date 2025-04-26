package top.caodong0225.jobs_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import top.caodong0225.jobs_server.entity.Jobs;
import top.caodong0225.jobs_server.vo.JobsVO;

import java.util.List;

/**
 * @author jyzxc
 */
public interface IJobsService extends IService<Jobs> {
    PageInfo<JobsVO> getJobsList(Integer pageNum,Integer pageSize,
                                Integer userId,
                                 String status,
                                 String startTime,
                                 String endTime,
                                 String name,
                                 String salaryDown,
                                 String salaryUp
    );

    boolean addJobs(Jobs jobs);
}
