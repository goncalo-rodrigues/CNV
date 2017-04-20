import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
    server.createContext("/r.html", new MyHandler());
    server.createContext("/images", new HttpHandler() {
      @Override
      public void handle(HttpExchange httpExchange) throws IOException {
        RequestImageThread rt = new RequestImageThread(httpExchange);
        Thread thread = new Thread(rt);
        thread.start();
      }
    });
    server.setExecutor(null); // creates a default executor
    server.start();
  }
}
