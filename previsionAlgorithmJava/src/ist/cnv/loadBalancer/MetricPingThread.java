package ist.cnv.loadBalancer;

import ist.cnv.worker.Worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * This thread continuously pings the workers to check if they are alive
 * The ping returns all current requests at that machine and their current metric value
 */
public class MetricPingThread implements Runnable {
    private final Worker worker;
    private RedirectRequest handler;
    private final static int PING_PERIOD = 5000;
    private final static int PING_TIMEOUT = 10000;

    public MetricPingThread(final Worker worker, RedirectRequest handler) {
        this.worker = worker;
        this.handler = handler;
    }

    @Override
    public void run() {
        boolean isDone = false;

        while (!isDone) {
            try {
                URL ws = new URL(new URL("http://" + worker.getAddress()), "metrics");
                HttpURLConnection wsc = (HttpURLConnection) ws.openConnection();
                wsc.setConnectTimeout(PING_TIMEOUT);
                wsc.setReadTimeout(PING_TIMEOUT);
                getResponse(wsc.getInputStream());
                Thread.sleep(PING_PERIOD);
            } catch (InterruptedException e) {
                // ignore
                System.out.println("Someone interruped me");
            } catch (Exception e) {
                System.out.println("Machine unreachable " + e.toString());
                e.printStackTrace();
                handler.removeWorker(worker);
                isDone = true;
            }
        }


    }

    private boolean getResponse(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader( new InputStreamReader(is));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            String[] vals = inputLine.split(",");
            String rid = vals[0];
            long val = Long.parseLong(vals[1]);
            worker.updateRequest(rid, val);
        }

        in.close();

        return true;
    }
}
