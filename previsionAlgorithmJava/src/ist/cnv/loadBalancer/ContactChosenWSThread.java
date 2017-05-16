package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ContactChosenWSThread implements Runnable {
    private HttpExchange httpEx = null;
    private String serverUrl = null;


    public ContactChosenWSThread(HttpExchange httpEx, String serverUrl) {
        this.httpEx = httpEx;
        this.serverUrl = serverUrl;
    }

    public void run() {
        String query = httpEx.getRequestURI().getQuery();
        String request = serverUrl + query;
        String responseBody = null;

        try {
            URL ws = new URL(request);
            HttpURLConnection wsc = (HttpURLConnection) ws.openConnection();
            responseBody = getResponse(wsc.getInputStream());
        } catch (MalformedURLException e) {
            // Not likely to happen
            e.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(responseBody == null) {
                    responseBody = "There was an error while processing the request!";
                    httpEx.sendResponseHeaders(500, responseBody.length());
                }
                else
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
                // Not likely to happen
                e.printStackTrace();
            }
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

