import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class LoadBalancer {
    public static void main(String[] args) throws Exception {

        // TODO: Must use port 80, changed for testing purposes
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/r.html", new RedirectRequest());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
