package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpExchange;
import ist.cnv.worker.Worker;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ContactChosenWSThread implements Runnable {
    private HttpExchange httpEx = null;
    private String serverUrl = null;
    private RedirectRequest handler = null;
    private Worker worker;


    public ContactChosenWSThread(HttpExchange httpEx, Worker worker, RedirectRequest handler) {
        this.httpEx = httpEx;
        this.serverUrl = worker.getFullAddress();
        this.handler = handler;
        this.worker = worker;
    }

    public void run() {
        String query = httpEx.getRequestURI().getQuery();
        String request = serverUrl + query;
        String responseBody = null;

        try {
            URL ws = new URL(request);
            HttpURLConnection wsc = (HttpURLConnection) ws.openConnection();
            // set timeout??
            responseBody = getResponse(wsc.getInputStream());
        } catch (MalformedURLException e) {
            // Not likely to happen
            e.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(responseBody == null) {
//                    responseBody = "There was an error while processing the request!";
//                    httpEx.sendResponseHeaders(500, responseBody.length());
                // try again, maybe remove this machine?
                handler.removeWorker(worker);
                handler.handle(httpEx);
            }
            else {
                try {
                    httpEx.sendResponseHeaders(200, responseBody.length());

                    String[] values = responseBody.split("\n");
                    String response = "<!doctype html><head></head><body>";
                    if (values.length >= 2) {
                        long metric = Long.parseLong(values[0]);
                        // TODO: use this metric to update table, possibly in a background thread
                        String imageUrl = values[1];
                        URL mergedUrl = new URL(new URL(serverUrl), imageUrl);
                        response += "Metric:" + metric + "<br>";
                        response += "<br> <a href=\"images/"+ mergedUrl + "\">See image here</a>";
                    } else {
                        response += "Something went wrong.";
                    }

                    response += "</body></html>";
                    httpEx.getResponseHeaders().set("Content-type", "text/html");
                    OutputStream os = httpEx.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        if (worker.getWorkload()==0 && worker.isDeleted()) {
            handler.terminateWorker(worker);
        }
    }

    private String getResponse(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader( new InputStreamReader(is));
        String inputLine;
        String fullResponse = "";

        while ((inputLine = in.readLine()) != null)
            fullResponse += inputLine + "\n";
        in.close();

        return fullResponse;
    }
}

