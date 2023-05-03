import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @className: test
 * @author: Bingfan Tian
 * @description: TODO
 * @date: 11/24/22 2:30 AM
 */
public class test {
    public static void main(String[] args) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(32);
        JedisPool pool = new JedisPool(config, "54.213.212.121", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.lpush("123", "world");
        }
    }
}
