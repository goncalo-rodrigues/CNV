package ist.cnv.loadBalancer;

import ist.cnv.worker.Worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * This thread pings a newborn worker until he responds
 *
 */

public class PingNewbornThread implements Runnable {
    private final Worker worker;
    private RedirectRequest handler;
    private final static int PING_PERIOD = 1000;
    private final static int PING_TIMEOUT = 10000;
    public PingNewbornThread(final Worker worker, RedirectRequest handler) {
        this.worker = worker;
        this.handler = handler;
    }

    @Override
    public void run() {
        boolean isDone = false;
        while (!isDone) {
            System.out.println("Pinging a newborn ( " + worker.getId() + " ... " +  worker.getAddress());
            try {
                URL ws = new URL(new URL("http://" + worker.getAddress()), "metrics");
                HttpURLConnection wsc = (HttpURLConnection) ws.openConnection();
                wsc.setConnectTimeout(PING_TIMEOUT);
                String responseBody = getResponse(wsc.getInputStream());
                if (responseBody.length() >= 0) {
                    isDone = true;
                }

            } catch (Exception e) {
                // ignore and try again!
            }

            try {
                if (!isDone)
                    Thread.sleep(PING_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        handler.spawnWorker(worker);


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
