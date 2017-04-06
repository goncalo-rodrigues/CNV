import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import raytracer.Main;

public class RequestThread implements Runnable {
  private HttpExchange t = null;

  public RequestThread(HttpExchange t) {
    this.t = t;
  }

  public void run() {
    try {
      String query = t.getRequestURI().getQuery();
      Map<String, String> args = queryToMap(query);
      String response = "Thread ID: " + Thread.currentThread().getId() + "\n###\n";
      for (Map.Entry e : args.entrySet()) {
        response += e.getKey() + "\t" + e.getValue() + "\n";
      }
      response += "###";

      try {
        if(args.containsKey("f") && args.containsKey("sc") && args.containsKey("sr") && args.containsKey("wc") &&
                args.containsKey("wr") && args.containsKey("coff") && args.containsKey("roff")) {

          String[] args_rt = {args.get("f"), "../" + args.get("f") + ".res",
                  args.get("sc"), args.get("sr"), args.get("wc"), args.get("wr"), args.get("coff"), args.get("roff")};
          raytracer.Main.main(args_rt);
        }

        else
          response += "\nThere is an argument missing from the request. Please try again.";
      } catch (InterruptedException e) {
        // Ignoring...
      }

      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    } catch(IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  private Map<String, String> queryToMap(String query) {
    Map<String, String> result = new HashMap<String, String>();
    if (query == null || query.length() == 0) {
      return result;
    }
    for (String param : query.split("&")) {
      String pair[] = param.split("=");
      if (pair.length>1) {
        result.put(pair[0], pair[1]);
      }else{
        result.put(pair[0], "");
      }
    }
    return result;
  }
}
