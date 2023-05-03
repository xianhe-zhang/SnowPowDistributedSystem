import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {
  private final static String QUEUE_NAME = "SkierServletPostQueue";
  private final static Integer NUM_THREADS = 512; //512;

  public static void main(String[] args) throws Exception {
    Gson gson = new Gson();
    ConnectionFactory factory = new ConnectionFactory();
//    ConcurrentHashMap<Integer, List<JsonObject>> map = new ConcurrentHashMap<>();
    JedisPool pool;
  // A2: Connect RabbitMQ
    if (Constant.test_local) {
      factory.setHost("localhost");
      factory.setPort(5672);
      factory.setUsername("guest");
      factory.setPassword("guest");
//      System.out.println("point1");
      // A3: Redis 代码
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setMaxTotal(512);
      pool = new JedisPool(poolConfig, "localhost", 6379);
    } else{
      factory.setHost("35.92.110.245");
      factory.setPort(5672);
      factory.setUsername("mario");
      factory.setPassword("mariobar");
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setMaxTotal(512);
      pool = new JedisPool(poolConfig, "34.221.41.113", 6379);
    }


    System.out.println("try to connect");
    Connection connection = factory.newConnection();
    System.out.println("connection successful");

    // test Jedis connection
    System.out.println(pool.getResource().ping());
    System.out.println("连接结束redis");

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try (Jedis jedis = pool.getResource()){
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          // 每个consumer最多一个把
          channel.basicQos(30);

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String key = String.valueOf(String.valueOf(jsonObject.get("skierID")));
            String time = String.valueOf(jsonObject.get("time"));
            String liftId = String.valueOf(jsonObject.get("liftID"));
            String info = time + "," + liftId;
            jedis.rpush(key, info);

            // 下面是A2的代码
//            if (map.contains(key)) {
//              map.get(key).add(jsonObject);
//            } else {
//              List<JsonObject> value = new ArrayList<>();
//              value.add(jsonObject);
//              map.put(key, value);
//            }
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);  // 学习要点1
          };
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
          });
        } catch (IOException e) {
          Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, e); //学习要点2
        }
      }
    };

    // Begin to start our threads.
    ExecutorService epool = Executors.newFixedThreadPool(NUM_THREADS);
    for (int i = 0; i < NUM_THREADS; i++) {
      epool.execute(runnable);
    }
    epool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
    pool.close();


//    for (int i=0; i<NUM_THREADS; i++){
//      Thread cons = new Thread(runnable);
//      cons.start();
//    }

  }
}
