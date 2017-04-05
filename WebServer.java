import java.net.InetSocketAddress;
import raytracer.Main;

import com.sun.net.httpserver.HttpServer;

public class WebServer {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
    server.createContext("/r.html", new MyHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }
}
