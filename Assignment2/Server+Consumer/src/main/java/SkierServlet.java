import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;

import entity.LiftRide;
import entity.ResponseMsg;
import entity.SkierVertical;
import entity.VerticalElement;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;


import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
//  private final
  private Gson gson = new Gson();
  // the pool is used to store channels
  private ObjectPool<Channel> pool;
  private final static String QUEUE_NAME = "SkierServletPostQueue";

  public void init() {
    this.pool = new GenericObjectPool<Channel>(new ConnectionPoolFactory());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//      ResponseMsg msg = new ResponseMsg("Missing Parameter");
      res.getWriter().write(gson.toJson(new ResponseMsg("Missing Parameter")));
      return;
    }

    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      if (urlParts.length==3) {
        List<VerticalElement> vrl = new ArrayList<VerticalElement>();
        vrl.add(new VerticalElement("string", 32));
        SkierVertical skierVertical = new SkierVertical(vrl);
        res.getWriter().write(gson.toJson(skierVertical));
      }
      else {
        res.getWriter().write("34507");
      }

    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    System.out.println("It's here");
//    System.out.println(req.toString());

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      ResponseMsg msg = new ResponseMsg("NOT FOUND");
      res.getWriter().write(gson.toJson(msg));
    } else {
      try {
        // below is Assignment2 Code
        StringBuilder sb = new StringBuilder();
        String s;
        while((s = req.getReader().readLine()) != null){
          sb.append(s);
        }

        System.out.println(sb.toString());
        LiftRide liftRide = gson.fromJson(sb.toString(), LiftRide.class);
        System.out.println("check point" + liftRide.toString());
        int skierID = Integer.parseInt(urlParts[7]);
        // To generate Json object
        JsonObject liftInfo = new JsonObject();
        liftInfo.addProperty("time", liftRide.getTime());
        liftInfo.addProperty("liftID", liftRide.getLiftID());
        liftInfo.addProperty("skierID", skierID);
//        System.out.println("First check : LiftInfo: " + liftInfo);


        Channel channel = null;
        // 尝试借channel并且publish相关的信息
        //        System.out.println("2");
        try {
//          System.out.println("2");
          channel = pool.borrowObject();
//          System.out.println("3");
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//          System.out.println("LiftInfo: " + liftInfo);
//          System.out.println("上面是liftInfo " );
          channel.basicPublish("", QUEUE_NAME, null, liftInfo.toString().getBytes());
        } catch (Exception e) {
          throw new RuntimeException("Unable to borrow from pool" + e.toString());
        } finally {
          try {
            if (channel != null) {
              System.out.println("channel return Done");
              pool.returnObject(channel);
            }
          } catch (Exception e){
            System.out.println("error when returning channel");
          }
        }
//        System.out.println("final");
        res.setStatus(HttpServletResponse.SC_CREATED);
//        System.out.println("res code:  " + res.getStatus());
      } catch (Exception ex) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // skiers/123/vertical
    // http://localhost:8080/a1server_war_exploded/skiers/1/seasons/2019/days/1/skiers/123
    // urlPath  = "/1/seasons/2019/days/1/skiers/123"
    // urlParts = [, 1, seasons, 2019, days, 1, skiers, 123]
    if(urlPath.length == 3){
      return urlPath[1].chars().allMatch(Character::isDigit) && urlPath[2].contains("vertical");
    } else if(urlPath.length == 8){
      return urlPath[1].chars().allMatch(Character::isDigit) && urlPath[2].equals("seasons") &&
          urlPath[3].chars().allMatch(Character::isDigit) && urlPath[4].equals("days") &&
          urlPath[5].chars().allMatch(Character::isDigit) && urlPath[6].equals("skiers") &&
          urlPath[7].chars().allMatch(Character::isDigit) && Integer.parseInt(urlPath[5]) >= 1 &&
          Integer.parseInt(urlPath[5])<=365;
    }
    return false;
  }
}
