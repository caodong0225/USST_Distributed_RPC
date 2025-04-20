package top.caodong0225.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import redis.clients.jedis.Jedis;
import top.caodong0225.protocol.RedisClient;

import java.io.IOException;
import java.util.*;

public class ProxyServlet extends HttpServlet {
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_RETRIES = 3;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. 解析请求路径
        String[] pathParts = parseRequestPath(req);
        if (pathParts == null) {
            sendError(resp, 400, "Invalid request path format");
            return;
        }

        String serviceName = pathParts[0];
        String targetPath = pathParts[1];

        // 2. 服务发现
        String targetUrl = discoverService(serviceName, req);
        if (targetUrl == null) {
            sendError(resp, 404, "Service not found");
            return;
        }

        // 3. 构建目标URL
        String fullUrl = targetUrl + targetPath;

        // 4. 转发请求（带重试机制）
        boolean success = forwardWithRetry(req, resp, fullUrl);

        if (!success) {
            sendError(resp, 503, "Service unavailable after retries");
        }
    }

    private String[] parseRequestPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            return null;
        }

        String[] parts = pathInfo.substring(1).split("/", 2);
        if (parts.length < 2) {
            return null;
        }

        return new String[]{parts[0], "/" + parts[1]};
    }

    private String discoverService(String serviceName, HttpServletRequest req) {
        try (Jedis jedis = RedisClient.getInstance().getJedis()) {
            // 获取服务实例集合
            Set<String> instances = jedis.smembers("rpc:service:" + serviceName);
            if (instances == null || instances.isEmpty()) {
                return null;
            }

            // 负载均衡策略（示例使用随机）
            List<String> instanceList = new ArrayList<>(instances);
            String selected = instanceList.get(new Random().nextInt(instanceList.size()));

            // 获取实例详情
            Map<String, String> instance = jedis.hgetAll(selected);
            return "http://" + instance.get("hostname") + ":" + instance.get("port");
        }
    }

    private boolean forwardWithRetry(HttpServletRequest srcReq,
                                     HttpServletResponse srcResp,
                                     String targetUrl) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                forwardRequest(srcReq, srcResp, targetUrl);
                return true;
            } catch (IOException e) {
                if (i == MAX_RETRIES - 1) {
                    System.out.println("Retry limit exceeded");
                }
            }
        }
        return false;
    }

    private void forwardRequest(HttpServletRequest srcReq,
                                HttpServletResponse srcResp,
                                String targetUrl) throws IOException {
        // 创建目标请求
        HttpRequestBase targetReq = createTargetRequest(srcReq, targetUrl);

        // 复制请求头
        Enumeration<String> headerNames = srcReq.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            targetReq.setHeader(name, srcReq.getHeader(name));
        }

        // 复制请求体（仅对可重复读取的请求）
        if (srcReq.getContentLength() > 0 &&
                (srcReq.getMethod().equals("POST") ||
                        srcReq.getMethod().equals("PUT") ||
                        srcReq.getMethod().equals("PATCH"))) {

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            srcReq.getInputStream().transferTo(buffer);
            ((HttpEntityEnclosingRequestBase)targetReq).setEntity(
                    new ByteArrayEntity(buffer.toByteArray())
            );
        }

        // 执行请求
        try (CloseableHttpResponse targetResp = httpClient.execute(targetReq)) {
            // 转发响应状态
            srcResp.setStatus(targetResp.getStatusLine().getStatusCode());

            // 转发响应头
            for (Header header : targetResp.getAllHeaders()) {
                srcResp.setHeader(header.getName(), header.getValue());
            }

            // 转发响应体
            targetResp.getEntity().writeTo(srcResp.getOutputStream());
        }
    }

    private HttpRequestBase createTargetRequest(HttpServletRequest srcReq,
                                                String targetUrl) {
        String method = srcReq.getMethod();
        switch (method) {
            case "GET":
                return new HttpGet(targetUrl);
            case "POST":
                return new HttpPost(targetUrl);
            case "PUT":
                return new HttpPut(targetUrl);
            case "DELETE":
                return new HttpDelete(targetUrl);
            default:
                return new HttpGet(targetUrl);
        }
    }

    private void sendError(HttpServletResponse resp, int code, String msg)
            throws IOException {
        resp.setStatus(code);
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", msg);
        mapper.writeValue(resp.getWriter(), error);
    }
}

