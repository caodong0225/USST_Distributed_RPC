package top.caodong0225.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;
import top.caodong0225.protocol.RedisClient;

public class HeartbeatServlet extends HttpServlet {
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        String instanceKey = req.getPathInfo().substring(1);

        try (Jedis jedis = RedisClient.getInstance().getJedis()) {
            // 更新心跳时间并续期
            jedis.hset(instanceKey, "lastHeartbeat", String.valueOf(System.currentTimeMillis()));
            jedis.expire(instanceKey, 90);
        }
    }
}
