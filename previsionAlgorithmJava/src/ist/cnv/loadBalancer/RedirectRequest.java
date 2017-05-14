package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ist.cnv.worker.AWSWorkerFactory;
import ist.cnv.worker.Worker;

import java.util.ArrayList;
import java.util.List;

public class RedirectRequest implements HttpHandler{
    private List<Worker> workers = new ArrayList<Worker>();
    private AWSWorkerFactory workerFactory;

    public RedirectRequest(){
        workerFactory = new AWSWorkerFactory();
    }

    @Override
    public void handle(HttpExchange t) {
        // Decides which WebServer will handle the request
        if(workers.size() == 0)
            workers.add(workerFactory.createWorker());

        String url = "http://" + choseWorkterToRequest();

        // Sends the request and waits for the result
        ContactChosenWSThread cct = new ContactChosenWSThread(t, url);
        Thread thread = new Thread(cct);
        thread.start();
    }

    private String choseWorkterToRequest(){//TODO set a proper chosing method
        return workers.get(0).getAddress();

    }
}
