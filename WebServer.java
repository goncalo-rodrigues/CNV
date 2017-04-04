import java.net.InetSocketAddress;
//import raytracer.Main;

import com.sun.net.httpserver.HttpServer;

public class WebServer {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/test", new MyHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }
}
