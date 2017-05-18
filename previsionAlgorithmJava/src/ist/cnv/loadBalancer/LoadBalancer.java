package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpServer;
import ist.cnv.scaler.Scaler;
import ist.cnv.worker.Worker;
import javafx.beans.value.WritableObjectValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class LoadBalancer {
    private static final List<Worker> workers = new ArrayList<>();
    private static RedirectRequest rr;

    public static void main(String[] args) throws Exception {

        // TODO: Must use port 80, changed for testing purposes
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        rr = new RedirectRequest(workers);
        server.createContext("/r.html", rr);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // creates a default executor
        server.start();

        // Creates the thread that will be responsible for managing the number of machines
        Scaler s = new Scaler(workers, rr);
        new Thread(s).start();
    }
}
