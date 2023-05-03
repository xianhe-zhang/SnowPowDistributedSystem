import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Processor.
 *
 * @className: Processor
 * @author: Bingfan Tian
 * @description: TODO
 * @date: 10 /23/22 11:41 PM
 */
public class Processor implements Runnable {

    private final String QUEUE_NAME = "SkierPostQueue";
    private final String RESORT_QUEUE_NAME = "ResortPostQueue";
    /**
     * The Gson.
     */
    private Gson gson = new Gson();
    /**
     * The Map.
     */
    private JedisPool pool;

    /**
     * The Connection.
     */
    private Connection connection;

    /**
     * Instantiates a new Processor.
     *
     * @param connection the connection
     * @param pool       the pool
     */
    public Processor(Connection connection, JedisPool pool) {
        this.connection = connection;
        this.pool = pool;
    }

    @Override
    public void run() {
        try (Jedis jedis = pool.getResource()) {
            final Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            channel.basicQos(1);

            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                String message = new String(delivery.getBody(), "UTF-8");
                // get json obj from the queue
                JsonObject json = gson.fromJson(message, JsonObject.class);
                // get key
                String skierId = String.valueOf(json.get("skierId"));
                String resortID = String.valueOf(json.get("resortId"));
                String seasonId = String.valueOf(json.get("seasonId"));
                String dayId = String.valueOf(json.get("dayId"));
                String liftId = String.valueOf(json.get("liftId"));
                String time = String.valueOf(json.get("time"));
                int vertical = Integer.parseInt(liftId) * 10;
                String info = resortID + "," + seasonId + "," + dayId + ","  + liftId + "," + time + "," + vertical;
                // For skier N, how many days have they skied this season?
                // update total days
                // hash => skierId => seasonId => days
                Map<String, String> skierFields = jedis.exists(skierId)? jedis.hgetAll(skierId): new HashMap<>();
                if (skierFields.containsKey(seasonId)) {
                    int preDays = Integer.parseInt(jedis.hget(skierId, seasonId));
                    jedis.hset(skierId, seasonId, String.valueOf(preDays + 1));
                } else {
                    jedis.hset(skierId, seasonId, "1");
                }
                // For skier N, what are the vertical totals for each ski day?
                // update total verticals
                // hash => skierId => dayId => total verticals
                if(skierFields.containsKey(dayId)) {
                    int preVertical = Integer.parseInt(jedis.hget(skierId, dayId));
                    jedis.hset(skierId, dayId, String.valueOf(preVertical + vertical));
                } else {
                    jedis.hset(skierId, dayId, String.valueOf(vertical));
                }

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });

        } catch (IOException e) {
            Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, e);
        }

    }
}
