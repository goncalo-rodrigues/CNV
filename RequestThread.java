import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

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
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    } catch(IOException e) {
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
