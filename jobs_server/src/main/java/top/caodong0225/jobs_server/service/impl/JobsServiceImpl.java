package top.caodong0225.jobs_server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.caodong0225.jobs_server.entity.Jobs;
import top.caodong0225.jobs_server.mapper.JobsMapper;
import top.caodong0225.jobs_server.service.IJobsService;
import top.caodong0225.jobs_server.vo.JobsVO;

import java.util.List;

/**
 * @author jyzxc
 */
@Service
public class JobsServiceImpl extends ServiceImpl<JobsMapper, Jobs> implements IJobsService {

    @Resource
    private JobsMapper jobsMapper;

    @Override
    public PageInfo<JobsVO> getJobsList(Integer pageNum,Integer pageSize, Integer userId, String status, String startTime, String endTime, String name, String salaryDown, String salaryUp) {
        // 启动分页（必须在查询方法前调用）
        PageHelper.startPage(pageNum, pageSize);

        // 执行查询（原有逻辑不变）
        List<JobsVO> jobsList = jobsMapper.getJobsList(userId, status, startTime,
                endTime, name, salaryDown,
                salaryUp);

        // 封装分页结果
        return new PageInfo<>(jobsList);
    }
    @Override
    public boolean addJobs(Jobs jobs) {
        return this.save(jobs);
    }
}
