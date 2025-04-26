package top.caodong0225.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import top.caodong0225.protocol.RedisClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.net.URL;

public class HealthChecker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(HealthChecker.class);
    private static final long CHECK_INTERVAL = 30_000; // 30秒检测一次
    private volatile boolean running = true;

    @Override
    public void run() {
        while (running) {
            try (Jedis jedis = RedisClient.getInstance().getJedis()) {
                // 1. 扫描所有服务实例
                Set<String> serviceKeys = jedis.keys("rpc:service:*");

                for (String serviceKey : serviceKeys) {
                    // 2. 获取该服务的所有实例
                    Set<String> instanceKeys = jedis.smembers(serviceKey);

                    // 3. 检查每个实例的健康状态
                    checkInstances(jedis, serviceKey, instanceKeys);
                }

            } catch (Exception e) {
                System.out.println("Health check failed"+e);
            }

            // 4. 休眠等待下次检测
            try {
                Thread.sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Health check failed"+e);
                break;
            }
        }
    }

    private void checkInstances(Jedis jedis, String serviceKey, Set<String> instanceKeys) {
        ExecutorService executor = Executors.newFixedThreadPool(10); // 线程池控制并发

        for (String instanceKey : instanceKeys) {
            executor.submit(() -> {
                try {
                    String[] keySplit = instanceKey.split(":");
                    // 取最后两个作为ip地址和端口
                    String host = keySplit[keySplit.length - 2];
                    String port = keySplit[keySplit.length - 1];
                    String path = "/health";

                    // 2. 构建健康检查URL
                    URL healthUrl = new URL("http://" + host + ":" + port + path);

                    // 3. 发送HTTP请求
                    boolean isHealthy = checkHealth(healthUrl);
                    updateInstanceStatus(jedis, instanceKey,serviceKey, isHealthy);

                } catch (Exception e) {
                    System.out.println("Health check failed"+e);
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private boolean checkHealth(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(2000);

            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 300;
        } catch (IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void updateInstanceStatus(Jedis jedis, String instanceKey,String serviceKey, boolean healthy) {
        String failureKey = instanceKey + ":failures"; // 失败计数器键
        try {
            if (healthy) {
                // 健康时：更新心跳时间并重置失败计数
                Transaction tx = jedis.multi();
                String[] keySplit = instanceKey.split(":");
                // 取最后两个作为ip地址和端口
                String hostname = keySplit[keySplit.length - 2];
                String port = keySplit[keySplit.length - 1];

                // 同时更新当前ip地址和端口
                tx.hset(instanceKey, "hostname", hostname);
                tx.hset(instanceKey, "port", port);
                tx.hset(instanceKey, "lastHeartbeat", String.valueOf(System.currentTimeMillis()));
                // 同时延长时间
                tx.expire(instanceKey, 90);
                tx.expire(serviceKey, 600);
                tx.del(failureKey); // 清除失败计数
                tx.exec();
            } else {
                // 不健康时：递增失败计数。设置过期时间

                long failures = jedis.hincrBy(failureKey, "count", 1);
                jedis.expire(failureKey, 60); // 设置过期时间为60

                // 达到3次失败后，删除实例
                if (failures >= 3) {
                    // 事务性删除实例
                    Transaction tx = jedis.multi();
                    tx.srem(serviceKey, instanceKey);  // 从服务集合移除
                    tx.del(instanceKey);               // 删除实例详情
                    tx.del(failureKey);                // 删除失败计数器
                    tx.exec();

                    log.warn("Removed instance after 3 failures: {}", instanceKey);
                } else {
                    // 记录失败日志
                    log.warn("Instance check failed ({} attempts): {}", failures, instanceKey);
                }
            }
        } catch (Exception e) {
            // Redis操作异常处理
            log.error("Redis operation failed for instance: {}", instanceKey, e);
            retryRedisOperation(jedis, instanceKey,serviceKey, healthy);
        }
    }

    // Redis操作重试方法
    private void retryRedisOperation(Jedis jedis, String instanceKey,String serviceKey, boolean healthy) {
        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                attempts++;
                updateInstanceStatus(jedis, instanceKey,serviceKey, healthy); // 递归重试
                return;
            } catch (Exception e) {
                if (attempts == maxRetries) {
                    log.error("Final retry failed for instance: {}", instanceKey, e);
                }
            }
        }
    }

    public void stop() {
        running = false;
    }
}
