package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.ArrayList;
import java.util.List;

public class RedirectRequest implements HttpHandler{
    private List<AvailableWebServer> ws = new ArrayList<AvailableWebServer>();

    @Override
    public void handle(HttpExchange t) {
        // Decides which WebServer will handle the request
        if(ws.size() == 0)
            // TODO: Check for the available instances

            // FIXME: Only for testing purposes
            ws.add(new AvailableWebServer("localhost"));


        // Sends the request and waits for the result
        // FIXME: For testing purposes, using the first element on the list
        ContactChosenWSThread cct = new ContactChosenWSThread(t, ws.get(0).getUrl());
        Thread thread = new Thread(cct);
        thread.start();
    }
}
