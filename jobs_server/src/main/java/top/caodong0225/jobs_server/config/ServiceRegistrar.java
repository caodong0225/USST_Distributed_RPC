package top.caodong0225.jobs_server.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@Component
public class ServiceRegistrar implements ApplicationListener<WebServerInitializedEvent>, DisposableBean {

    @Value("${registry.center.url}")
    private String registryUrl;

    @Value("${spring.application.name}")
    private String serviceName;

    private final RestTemplate restTemplate = new RestTemplate();

    private String key;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        String host = getHostAddress();
        int port = event.getWebServer().getPort();
        this.key = serviceName + ":" + host + ":" + port;
        // 注册服务
        URL registration = new URL(serviceName, host, port);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                registryUrl + "/register",
                registration,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to register service: " + response.getStatusCode());
        }
    }


    @Override
    public void destroy() {
        // 获取结果
        ResponseEntity<Map> response = restTemplate.exchange(
                registryUrl + "/unregister/" + this.key,
                HttpMethod.DELETE,
                null,
                Map.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to unregister service: " + response.getStatusCode());
        }
    }

    private String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}