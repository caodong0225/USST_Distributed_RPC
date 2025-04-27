package top.caodong0225.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import redis.clients.jedis.Jedis;
import top.caodong0225.protocol.RedisClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ProxyServlet extends HttpServlet {
    private static final CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(5000)    // 连接超时 5秒
                    .setSocketTimeout(15000)    // 数据传输超时 15秒
                    .build())
            .disableRedirectHandling()      // 禁用自动重定向
            .build();
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

        // 3. 构建完整目标URL
        String fullUrl = targetUrl + targetPath + (req.getQueryString() != null ? "?" + req.getQueryString() : "");

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

        // 处理形如 /serviceName/rest/of/path 的路径
        String[] parts = pathInfo.substring(1).split("/", 2);
        if (parts.length < 1) {
            return null;
        }

        String serviceName = parts[0];
        String targetPath = parts.length > 1 ? "/" + parts[1] : "/";
        return new String[]{serviceName, targetPath};
    }

    private String discoverService(String serviceName, HttpServletRequest req) {
        try (Jedis jedis = RedisClient.getInstance().getJedis()) {
            Set<String> instances = jedis.smembers("rpc:service:" + serviceName);
            if (instances == null || instances.isEmpty()) {
                return null;
            }

            // 随机负载均衡
            List<String> instanceList = new ArrayList<>(instances);
            String selected = instanceList.get(new Random().nextInt(instanceList.size()));

            Map<String, String> instance = jedis.hgetAll(selected);
            return String.format("http://%s:%s", instance.get("hostname"), instance.get("port"));
        }
    }

    private boolean forwardWithRetry(HttpServletRequest srcReq,
                                     HttpServletResponse srcResp,
                                     String targetUrl) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                forwardRequest(srcReq, srcResp, targetUrl);
                return true;
            } catch (Exception e) {
                if (i == MAX_RETRIES - 1) {
                    System.err.println("Forward failed after " + MAX_RETRIES + " retries: " + e.getMessage());
                }
            }
        }
        return false;
    }

    private void forwardRequest(HttpServletRequest srcReq,
                                HttpServletResponse srcResp,
                                String targetUrl) throws IOException {
        // 创建目标请求对象
        HttpRequestBase targetRequest = createTargetRequest(srcReq, targetUrl);

        // 复制请求头（排除 Hop-by-Hop 头和 Content-Length）
        Enumeration<String> headerNames = srcReq.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (isHopByHopHeader(headerName) || "Content-Length".equalsIgnoreCase(headerName)) {
                continue; // 跳过不需要的请求头
            }
            targetRequest.setHeader(headerName, srcReq.getHeader(headerName));
        }

        // 复制请求体（仅对包含实体的方法）
        if (targetRequest instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase) targetRequest;
            InputStream requestBodyStream = srcReq.getInputStream();

            // 显式设置内容类型和编码
            String contentType = srcReq.getContentType();
            Header contentTypeHeader = new BasicHeader("Content-Type", contentType != null ? contentType : "application/octet-stream");
            entityRequest.setHeader(contentTypeHeader);

            // 使用 ByteArrayEntity 确保完整读取请求体
            byte[] bodyBytes = IOUtils.toByteArray(requestBodyStream);
            entityRequest.setEntity(new ByteArrayEntity(bodyBytes));
        }


        // 执行请求并获取响应
        try {
            CloseableHttpResponse targetResponse = httpClient.execute(targetRequest);
            // 1. 转发状态码
            srcResp.setStatus(targetResponse.getStatusLine().getStatusCode());

            // 2. 转发响应头（过滤 Hop-by-Hop 头）
            Arrays.stream(targetResponse.getAllHeaders())
                    .filter(header -> !isHopByHopHeader(header.getName()))
                    .forEach(header -> srcResp.setHeader(header.getName(), header.getValue()));

            // 3. 转发响应体
            HttpEntity entity = targetResponse.getEntity();
            if (entity != null) {
                try (InputStream content = entity.getContent()) {
                    IOUtils.copy(content, srcResp.getOutputStream());
                }
            }

            // 确保实体内容被消费
            EntityUtils.consume(entity);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }

    private HttpRequestBase createTargetRequest(HttpServletRequest srcReq,
                                                String targetUrl) {
        String method = srcReq.getMethod().toUpperCase();
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
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
    }

    private boolean isHopByHopHeader(String headerName) {
        // 允许 Content-* 和 Host 头
        if (headerName.startsWith("Content-") || "Host".equalsIgnoreCase(headerName)) {
            return false;
        }
        final Set<String> hopByHopHeaders = new HashSet<>(Arrays.asList(
                "Connection", "Keep-Alive", "Proxy-Authenticate",
                "Proxy-Authorization", "TE", "Trailers", "Transfer-Encoding", "Upgrade"
        ));
        return hopByHopHeaders.contains(headerName);
    }

    private void sendError(HttpServletResponse resp, int code, String msg)
            throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json");
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", msg);
        mapper.writeValue(resp.getWriter(), error);
    }
}