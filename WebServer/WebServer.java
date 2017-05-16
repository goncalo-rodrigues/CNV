import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
  public static void main(String[] args) throws Exception {
    final HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
    server.createContext("/r.html", new MyHandler());
    server.createContext("/images", new HttpHandler() {
      @Override
      public void handle(HttpExchange httpExchange) throws IOException {
        RequestImageThread rt = new RequestImageThread(httpExchange);
        Thread thread = new Thread(rt);
        thread.start();
      }
    });

    server.createContext("/metrics", new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = "";
            for (Map.Entry<String, Long> kv: RequestThread.requestIdToThreadId.entrySet()) {
                response += kv.getKey() + "," + StatisticsDotMethodTool.getMetric(kv.getValue()) + "\n";
            }
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    });
    server.setExecutor(null); // creates a default executor
    server.start();
  }
}
