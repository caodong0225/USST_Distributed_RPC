package top.caodong0225.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import redis.clients.jedis.Jedis;
import top.caodong0225.loadbalance.LoadBalance;
import top.caodong0225.protocol.RedisClient;

import java.io.IOException;
import java.util.*;

/**
 * <p>
 *     服务查询接口
 * </p>
 * @author jyzxc
 */
public class LookupServlet extends HttpServlet {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 解析请求路径 /lookup/<serviceName>/<methodPath>
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(400, "Service name is required in path");
            return;
        }

        String[] pathParts = pathInfo.substring(1).split("/", 2);
        if (pathParts.length < 1) {
            resp.sendError(400, "Invalid path format");
            return;
        }

        String serviceName = pathParts[0];
        String methodPath = pathParts.length > 1 ? pathParts[1] : "";

        // 从Redis获取服务实例
        String redisKey = "rpc:service:" + serviceName;
        try (Jedis jedis = RedisClient.getInstance().getJedis()) {
            Set<String> addressSet = jedis.smembers(redisKey);

            if (addressSet == null || addressSet.isEmpty()) {
                resp.sendError(404, "Service not found");
                return;
            }

            // 负载均衡选择实例
            List<String> instances = new ArrayList<>(addressSet);
            String selectedInstance = LoadBalance.random(instances);

            // 构建目标URL
            String targetUrl = "http://" + selectedInstance + "/" + methodPath;

            // 转发请求
            forwardRequest(req, resp, targetUrl);
        } catch (Exception e) {
            resp.sendError(500, "Internal server error: " + e.getMessage());
        }
    }

    private void forwardRequest(HttpServletRequest srcReq, HttpServletResponse srcResp, String targetUrl) throws IOException {
        // 创建目标请求
        HttpRequestBase targetRequest = createTargetRequest(srcReq, targetUrl);

        // 复制请求头
        Enumeration<String> headerNames = srcReq.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> values = srcReq.getHeaders(name);
            while (values.hasMoreElements()) {
                targetRequest.addHeader(name, values.nextElement());
            }
        }

        // 对于有请求体的方法(POST, PUT等)，复制请求体
        if (srcReq.getContentLength() > 0 && (srcReq.getMethod().equals("POST") ||
                srcReq.getMethod().equals("PUT") ||
                srcReq.getMethod().equals("PATCH"))) {
            ((HttpEntityEnclosingRequestBase)targetRequest).setEntity(
                    new ByteArrayEntity(srcReq.getInputStream().readAllBytes())
            );
        }

        // 执行请求并转发响应
        try (CloseableHttpResponse targetResponse = httpClient.execute(targetRequest)) {
            // 设置响应状态
            srcResp.setStatus(targetResponse.getStatusLine().getStatusCode());

            // 复制响应头
            for (Header header : targetResponse.getAllHeaders()) {
                srcResp.addHeader(header.getName(), header.getValue());
            }

            // 复制响应体
            if (targetResponse.getEntity() != null) {
                targetResponse.getEntity().writeTo(srcResp.getOutputStream());
            }
        }
    }

    private HttpRequestBase createTargetRequest(HttpServletRequest srcReq, String targetUrl) {
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
            case "PATCH":
                return new HttpPatch(targetUrl);
            case "HEAD":
                return new HttpHead(targetUrl);
            case "OPTIONS":
                return new HttpOptions(targetUrl);
            default:
                return new HttpGet(targetUrl);
        }
    }

    @Override
    public void destroy() {
        try {
            httpClient.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        super.destroy();
    }
}