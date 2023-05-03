import com.google.gson.Gson;
import entity.LiftRide;
import entity.ResponseMsg;
import entity.SkierVertical;
import entity.VerticalElement;


import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
//  private final
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    Gson gson = new Gson();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//      ResponseMsg msg = new ResponseMsg("Missing Parameter");
      res.getWriter().write(gson.toJson(new ResponseMsg("Missing Parameter")));
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      if (urlParts.length==3) {


        List<VerticalElement> vrl = new ArrayList<VerticalElement>();
        vrl.add(new VerticalElement("string", 32));
        SkierVertical skierVertical = new SkierVertical(vrl);
        res.getWriter().write(gson.toJson(skierVertical));
      }
      else {
        res.getWriter().write(34507);
      }

    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    Gson gson = new Gson();
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      ResponseMsg msg = new ResponseMsg("NOT FOUND");
      res.getWriter().write(gson.toJson(msg));
    } else {
      try {
        // do any sophisticated processing with urlParts which contains all the url params
        // TODO: process url params in `urlParts`
        LiftRide liftRide = new LiftRide(217,21,5000);
        ResponseMsg msg = new ResponseMsg("Successful Created");
        String msgJsonString = gson.toJson(msg);
        PrintWriter out = res.getWriter();
        out.print(msgJsonString);
        out.flush();
        res.setStatus(HttpServletResponse.SC_CREATED);
      } catch (Exception ex) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        ResponseMsg msg = new ResponseMsg("Failed Created");
        String msgJsonString = gson.toJson(msg);
        PrintWriter out = res.getWriter();
        out.print(msgJsonString);
        out.flush();
      }
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
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
