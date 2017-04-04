import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class MyHandler implements HttpHandler {
  @Override
  public void handle(HttpExchange t) {
    RequestThread rt = new RequestThread(t);
    Thread thread = new Thread(rt);
    thread.start();
  }
}
