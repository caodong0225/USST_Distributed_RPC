package top.caodong0225.jobs_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author jyzxc
 */
@RestController
@RequestMapping("/health")
public class TestController {
    @GetMapping("")
    public String test() {
        return "Hello World!";
    }
}
