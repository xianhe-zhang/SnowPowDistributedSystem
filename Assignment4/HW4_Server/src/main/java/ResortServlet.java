import Models.LiftRide;
import RMQPool.RMQChannelFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * The Resort servlet.
 */
public class ResortServlet extends HttpServlet {

    private static final int DAY_MIN = 1;
    private static final int DAY_MAX = 366;
    private JedisPool Jedis_Pool;
    private final String DB_HOST = "localhost";
    private final int DB_PORT = 6379;


    @Override
    public void init() {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(128);
            this.Jedis_Pool = new JedisPool(config, DB_HOST, DB_PORT);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Incorrect parameters or Invalid Resort ID supplied" );
        } else {
            // GET/resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
            Jedis jedis = Jedis_Pool.getResource();
            String resortID = urlParts[1];
            String seasonID = urlParts[3];
            String dayID = urlParts[5];
            String numOfSkiers = null;

            String searchKey = resortID + "-" + dayID;

            numOfSkiers = String.valueOf(jedis.scard(searchKey));
            if (numOfSkiers != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("It works!\n" + "The total number of unique skiers at Resort " + resortID
                        + " at season " + seasonID + " day " + dayID + " is: " + numOfSkiers);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Cannot find any record for resort " + resortID
                        + " at season " + seasonID);
            }
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        if(urlPath.length != 7) {
            return false;
        } else {
            return isNumeric(urlPath[1]) && urlPath[2].equals("seasons") &&
                    isNumeric(urlPath[3]) && urlPath[3].length() == 4 && urlPath[4].equals("day") &&
                    isNumeric(urlPath[5]) &&
                    Integer.parseInt(urlPath[5]) >= DAY_MIN &&
                    Integer.parseInt(urlPath[5]) <= DAY_MAX &&
                    urlPath[6].equals("skiers");
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

}
