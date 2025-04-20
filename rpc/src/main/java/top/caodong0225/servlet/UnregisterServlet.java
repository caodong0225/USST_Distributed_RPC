package top.caodong0225.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import top.caodong0225.protocol.RedisClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class UnregisterServlet extends HttpServlet {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Map<String, Object> response = new LinkedHashMap<>();

        try (Jedis jedis = RedisClient.getInstance().getJedis()) {
            // 1. 解析路径参数
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                buildErrorResponse(resp, response, 400, "Missing registration ID");
                return;
            }

            String registrationId = pathInfo.substring(1); // 去掉开头的斜杠
            String[] parts = registrationId.split(":");
            if (parts.length != 3) {
                buildErrorResponse(resp, response, 400, "Invalid registration ID format");
                return;
            }

            // 2. 构造Redis Key
            String serviceName = parts[0];
            String serviceKey = "rpc:service:" + serviceName;
            String instanceKey = "rpc:instance:" + registrationId;

            // 3. 执行删除操作（事务保证原子性）
            Transaction tx = jedis.multi();
            tx.srem(serviceKey, instanceKey);
            tx.del(instanceKey);
            tx.exec();

            // 4. 构建响应
            response.put("code", 200);
            response.put("message", "Service unregistered successfully");
            response.put("timestamp", System.currentTimeMillis());
            resp.setStatus(200);
        } catch (Exception e) {
            buildErrorResponse(resp, response, 500, "Internal server error: " + e.getMessage());
        }

        mapper.writeValue(resp.getWriter(), response);
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