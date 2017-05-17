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
    private long prevision;
    private static int counter = 0;
    private String f;
    private int sc,sr,wc,wr,coff,roff;

    public void setParameters(String f, int sc, int sr, int wc, int wr, int coff, int roff) {
        this.f=f;this.sc=sc;this.sr=sr;this.wc=wc;this.wr=wr;this.coff=coff;this.roff=roff;
    }

    public ContactChosenWSThread(HttpExchange httpEx, Worker worker, RedirectRequest handler, long prevision) {
        this.httpEx = httpEx;
        this.serverUrl = worker.getFullAddress();
        this.handler = handler;
        this.worker = worker;
        this.prevision = prevision;
    }

    public void run() {
        String query = httpEx.getRequestURI().getQuery();
        String request = serverUrl + "?" + query;
        System.out.println(query);
        String responseBody = null;
        String rid = String.valueOf(counter++);
        try {
            worker.addRequest(rid, prevision);
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
                System.out.println("responseBody==null");
                handler.removeWorker(worker);
                handler.handle(httpEx);
            }
            else {
                try {


                    String[] values = responseBody.split("\n");
                    String response = "<!doctype html><head></head><body>";
                    if (values.length >= 2) {
                        try {
                            long metric = Long.parseLong(values[0]);
                            String imageUrl = values[1];
                            URL mergedUrl = new URL(new URL(serverUrl), imageUrl);
                            response += "Metric:" + metric + "<br>";
                            response += "<br> <a href=\"" + mergedUrl + "\">See image here</a>";
                            handler.update(f,sc,sr,wc,wr,coff,roff,metric);
                        } catch (NumberFormatException e) {
                            response += responseBody;
                        }

                    } else {
                        response += "Something went wrong.\n" + responseBody;
                    }

                    response += "</body></html>";
                    httpEx.sendResponseHeaders(200, response.length());
                    httpEx.getResponseHeaders().set("Content-type", "text/html");
                    OutputStream os = httpEx.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        worker.removeRequest(rid);
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

