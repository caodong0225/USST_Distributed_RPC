package top.caodong0225.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import top.caodong0225.common.URL;
import top.caodong0225.protocol.RedisClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 注册服务接口
 * POST /register
 * Content-Type: application/json
 * <p>
 * {
 * "serviceName": "userService",
 * "hostname": "192.168.1.100",
 * "port": 8080
 * }
 * </p>
 *
 * @author jyzxc
 */
public class RegisterServlet extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Map<String, Object> response = new LinkedHashMap<>();

        try (Jedis jedis = RedisClient.getInstance().getJedis()) {
            // 参数校验
            URL info = validateRequest(req);

            // 构建Redis Key
            String serviceKey = buildServiceKey(info.getServiceName());
            String instanceKey = buildInstanceKey(info);

            // 注册服务实例
            registerService(jedis, serviceKey, instanceKey, info);

            buildSuccessResponse(response);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            buildErrorResponse(resp, response, 400, e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            buildErrorResponse(resp, response, 500, "Internal server error");
        }

        mapper.writeValue(resp.getWriter(), response);
    }

    private URL validateRequest(HttpServletRequest req) throws IOException {
        URL info = mapper.readValue(req.getInputStream(), URL.class);

        if (info.getServiceName() == null || info.getServiceName().isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        if (info.getHostname() == null || info.getHostname().isEmpty()) {
            throw new IllegalArgumentException("Hostname is required");
        }
        if (info.getPort() == null || info.getPort() <= 0) {
            throw new IllegalArgumentException("Invalid port number");
        }

        return info;
    }

    private String buildServiceKey(String serviceName) {
        return "rpc:service:" + serviceName;
    }

    private String buildInstanceKey(URL info) {
        return "rpc:instance:" + info.getServiceName() + ":"
                + info.getHostname() + ":" + info.getPort();
    }

    private void registerService(Jedis jedis, String serviceKey,
                                 String instanceKey, URL info) {
        // 使用Hash存储实例详情
        Map<String, String> instanceData = new ConcurrentHashMap<>();
        instanceData.put("hostname", info.getHostname());
        instanceData.put("port", String.valueOf(info.getPort()));
        instanceData.put("lastHeartbeat", String.valueOf(System.currentTimeMillis()));

        // 使用事务操作保证原子性
        Transaction tx = jedis.multi();
        tx.hmset(instanceKey, instanceData);
        tx.expire(instanceKey, 90);  // 设置90秒过期时间
        // 设置过期时间
        tx.sadd(serviceKey, instanceKey);
        tx.expire(serviceKey, 600);
        tx.exec();
    }

    private void buildSuccessResponse(Map<String, Object> response) {
        response.put("code", 200);
        response.put("message", "Registration successful");
        response.put("timestamp", System.currentTimeMillis());
    }

    private void buildErrorResponse(HttpServletResponse resp,
                                    Map<String, Object> response,
                                    int code, String message) {
        resp.setStatus(code);
        response.put("code", code);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
    }
}