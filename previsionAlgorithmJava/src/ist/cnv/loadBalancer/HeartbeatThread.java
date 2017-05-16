package ist.cnv.loadBalancer;

import ist.cnv.worker.Worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by goncalo on 16-05-2017.
 */
public class HeartbeatThread implements Runnable {
    private final Worker worker;
    private RedirectRequest handler;
    private final static int PING_PERIOD = 5000;
    public HeartbeatThread(final Worker worker, RedirectRequest handler) {
        this.worker = worker;
        this.handler = handler;
    }

    @Override
    public void run() {
        boolean isDone = false;
        while (!isDone) {
            try {
                URL ws = new URL(new URL(worker.getAddress()), "ping");
                HttpURLConnection wsc = (HttpURLConnection) ws.openConnection();
                String responseBody = getResponse(wsc.getInputStream());
                if (responseBody.length() <= 0) {
                    // didn't respond :(
                    handler.removeWorker(worker);
                    isDone = true;
                }
                Thread.sleep(PING_PERIOD);
            } catch (InterruptedException e) {
                // ignore
                System.out.println("Someone interruped me");
            } catch (Exception e) {
                handler.removeWorker(worker);
                isDone = true;
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
