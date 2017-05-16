package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpServer;
import ist.cnv.worker.Worker;
import javafx.beans.value.WritableObjectValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancer {
    private static final List<Worker> workers = new ArrayList<>();
    public static void main(String[] args) throws Exception {

        // TODO: Must use port 80, changed for testing purposes
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/r.html", new RedirectRequest(workers));
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
