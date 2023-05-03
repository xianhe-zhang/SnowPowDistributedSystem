import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {
  private final static String QUEUE_NAME = "SkierServletPostQueue";
  private final static Integer NUM_THREADS = 10; //512;

  public static void main(String[] args) throws Exception {
    Gson gson = new Gson();
    ConnectionFactory factory = new ConnectionFactory();
    ConcurrentHashMap<Integer, List<JsonObject>> map = new ConcurrentHashMap<>();

//    factory.setHost("localhost");
    factory.setHost("35.90.118.182");
    factory.setPort(5672);
    factory.setUsername("mario");
    factory.setPassword("mariobar");
//    factory.setUsername("guest");
//    factory.setPassword("guest");
    System.out.println("try to connect");
    Connection connection = factory.newConnection();
    System.out.println("connection successful");

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          // 每个consumer最多一个把
          channel.basicQos(30);

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            System.out.println("1");
            String message = new String(delivery.getBody(), "UTF-8");
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
//            System.out.println(jsonObject.toString());
            Integer key = Integer.valueOf(String.valueOf(jsonObject.get("skierID")));
            if (map.contains(key)) {
              map.get(key).add(jsonObject);
            } else {
              List<JsonObject> value = new ArrayList<>();
              value.add(jsonObject);
              map.put(key, value);
            }
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
    ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
    for (int i = 0; i < NUM_THREADS; i++) {
      pool.execute(runnable);
    }

//    for (int i=0; i<NUM_THREADS; i++){
//      Thread cons = new Thread(runnable);
//      cons.start();
//    }

  }
}
