package top.caodong0225.protocol;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {

    private static RedisClient instance;
    private final JedisPool jedisPool;

    private RedisClient(String host, int port) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(50);
        config.setMaxIdle(10);
        config.setMinIdle(1);
        config.setTestOnBorrow(true);
        this.jedisPool = new JedisPool(config, host, port);
    }

    // init method - call this once at startup
    public static void init(String host, int port) {
        if (instance == null) {
            instance = new RedisClient(host, port);
        }
    }

    // global access point
    public static RedisClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RedisClient not initialized. Call init(host, port) first.");
        }
        return instance;
    }

    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    public void close() {
        jedisPool.close();
    }
}

