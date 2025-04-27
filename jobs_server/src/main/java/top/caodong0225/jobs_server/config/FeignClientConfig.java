package top.caodong0225.jobs_server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.caodong0225.jobs_server.service.IRemoteService;

/**
 * @author jyzxc
 */
@Configuration
public class FeignClientConfig {
    @Value("${registry.center.url}")
    private String registryUrl;

    @Bean
    public IRemoteService remoteService() {
        // 创建 ObjectMapper 并注册时间模块
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .target(IRemoteService.class, registryUrl);
    }
}
