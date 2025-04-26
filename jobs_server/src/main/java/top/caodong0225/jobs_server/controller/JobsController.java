package top.caodong0225.jobs_server.controller;

import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import top.caodong0225.jobs_server.dto.GeneralDataResponseDTO;
import top.caodong0225.jobs_server.entity.Jobs;
import top.caodong0225.jobs_server.service.IJobsService;
import top.caodong0225.jobs_server.vo.JobsVO;

import java.time.LocalDateTime;

/**
 * @author jyzxc
 */
@RestController
@RequestMapping("/jobs")
public class JobsController {
    private final IJobsService jobsService;
    public JobsController(IJobsService jobsService) {
        this.jobsService = jobsService;
    }

    @GetMapping("/list")
    public GeneralDataResponseDTO<PageInfo<JobsVO>> getJobsList(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "userId", required = false) Integer userId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "startAt", required = false) LocalDateTime startAt,
            @RequestParam(name = "endAt", required = false) LocalDateTime endAt,
            @RequestParam(name = "salaryDown", required = false) Integer salaryDown,
            @RequestParam(name = "salaryUp", required = false) Integer salaryUp,
            @RequestParam(name = "status", required = false) String status
    ){
        return new GeneralDataResponseDTO<>(jobsService.getJobsList(
                pageNum,
                pageSize,
                userId,
                status,
                startAt != null ? startAt.toString() : null,
                endAt != null ? endAt.toString() : null,
                name,
                salaryDown != null ? salaryDown.toString() : null,
                salaryUp != null ? salaryUp.toString() : null
        ));
    }

    @PostMapping("/add")
    public GeneralDataResponseDTO<Jobs> addJob(
            @Valid @RequestBody Jobs job,
            HttpServletRequest request
    ) {
        if(request.getSession().getAttribute("role") != "super-admin") {
            return new GeneralDataResponseDTO<>(400, "没有权限");
        }
        job.setId(null);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        job.setUserId((Integer) request.getSession().getAttribute("userId"));
        job.setStatus("ongoing");
        if(jobsService.addJobs(job)){
            return new GeneralDataResponseDTO<>(200, "添加成功");
        } else {
            return new GeneralDataResponseDTO<>(400, "添加失败");
        }
    }
}
