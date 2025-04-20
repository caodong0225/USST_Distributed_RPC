package top.caodong0225.server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobsController {
    @RequestMapping("/test")
    public String test() {
        return "Hello World!";
    }
}
