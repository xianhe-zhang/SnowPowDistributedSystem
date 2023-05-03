import Models.LiftRide;
import RMQPool.RMQChannelFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * The Skier servlet.
 */
@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

    private static final int DAY_MIN = 1;
    private static final int DAY_MAX = 366;

    private final static String SKIER_QUEUE_NAME = "SkierPostQueue";
    private final static String RESORT_QUEUE_NAME = "ResortPostQueue";

    private Gson gson  = new Gson();

    private ObjectPool<Channel> pool;
    private ConnectionFactory factory;
    private JedisPool Jedis_Pool;

    private final String RMQ_HOST = "localhost";
    private final int RMQ_PORT = 5672;

    private final String DB_HOST = "localhost";
    private final int DB_PORT = 6379;


    @Override
    public void init() {
        try {
            this.factory = new ConnectionFactory();
            factory.setHost(RMQ_HOST);
            factory.setPort(RMQ_PORT);
            factory.setUsername("guest");
            factory.setPassword("guest");
            this.pool = new GenericObjectPool<Channel>(new RMQChannelFactory(factory.newConnection()));
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(128);
            this.Jedis_Pool = new JedisPool(config, DB_HOST, DB_PORT);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("incorrect parameters");
        } else {
            Jedis jedis = Jedis_Pool.getResource();
            Map<String, String> skierFields;
            if (!urlParts[urlParts.length - 1].equals("vertical")) {
                // GET/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
                String seasonID = urlParts[3];
                String dayID = urlParts[5];
                String skierID = urlParts[7];
                String dayVertical = null;

                skierFields = jedis.hgetAll(skierID);
                if (!skierFields.isEmpty() && skierFields.containsKey(dayID)) {
                    dayVertical = jedis.hget(skierID, dayID);
                }
                if (dayVertical != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("It works!\n" + "The total vertical for skier " + skierID + " at day "
                            + dayID + " season " + seasonID + " is: " + dayVertical);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("Cannot find any record for skier " + skierID + " at day " + dayID
                            + " season " + seasonID);
                }
            } else {
                // GET/skiers/{skierID}/vertical
                int totalVertical = 0;
                String skierID = urlParts[1];
                skierFields = jedis.hgetAll(skierID);
                if (!skierFields.isEmpty()) {
                    for (String key :skierFields.keySet()) {
                        if(!key.equals("2022"))
                            totalVertical += Integer.parseInt(skierFields.get(key));
                    }
                }
                if (totalVertical != 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("It works!\n" + "The total vertical for skier " + skierID
                            + " in current season is: " + totalVertical);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("Cannot find any record for skier " + skierID + " in current season!");
                }
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getOutputStream().println("incorrect parameters");
        } else {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            LiftRide liftRide = gson.fromJson(sb.toString(), LiftRide.class);
            if(liftRide != null && isBodyValid(liftRide)) {
                JsonObject liftInfo = new JsonObject();
                liftInfo.addProperty("resortId", Integer.valueOf(urlParts[1]));
                liftInfo.addProperty("seasonId", Integer.valueOf(urlParts[3]));
                liftInfo.addProperty("dayId", Integer.valueOf(urlParts[5]));
                liftInfo.addProperty("skierId", Integer.valueOf(urlParts[7]));
                liftInfo.addProperty("time", liftRide.getTime());
                liftInfo.addProperty("liftId", liftRide.getLiftID());
                if (sendToQueue(liftInfo)) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().write(gson.toJson("success"));
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("failed");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("incorrect request body");
            }
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        if(urlPath.length != 8 && urlPath.length != 3) {
            return false;
        } else {
            if (urlPath.length == 8) {
                return isNumeric(urlPath[1]) && urlPath[2].equals("seasons") &&
                        isNumeric(urlPath[3]) && urlPath[3].length() == 4 && urlPath[4].equals("days") &&
                        isNumeric(urlPath[5]) &&
                        Integer.parseInt(urlPath[5]) >= DAY_MIN &&
                        Integer.parseInt(urlPath[5]) <= DAY_MAX &&
                        urlPath[6].equals("skiers") && isNumeric(urlPath[7]);
            } else {
                return isNumeric(urlPath[1]) && urlPath[2].equals("vertical") ;
            }
        }
    }

    private boolean isNumeric(String s) {
        if(s == null || s.equals("")) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ignored) { }
        return false;
    }

    private boolean isBodyValid(LiftRide liftRide) {
        if(liftRide.getTime() == null || liftRide.getLiftID() == null)
            return false;
        return true;
    }

    private boolean sendToQueue(JsonObject liftInfo) {
        Channel channel = null;
        try {
            channel = pool.borrowObject();
            channel.queueDeclare(SKIER_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(RESORT_QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", SKIER_QUEUE_NAME, null,
                    liftInfo.toString().getBytes(StandardCharsets.UTF_8));
            channel.basicPublish("", RESORT_QUEUE_NAME, null,
                    liftInfo.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            System.out.println("Unable to borrow channel from pool" + e.toString());
            return false;
        } finally {
            if (channel != null) {
                try {
                    pool.returnObject(channel);
                } catch (Exception e) {
                    System.out.println("Cannot return channel");
                }
            }
        }
    }
}
