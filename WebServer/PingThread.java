import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by goncalo on 16-05-2017.
 */
public class PingThread implements Runnable {
    private HttpExchange t = null;

    public PingThread(HttpExchange t) {
        this.t = t;
    }

    @Override
    public void run() {
        String response = "pong";
        OutputStream os = t.getResponseBody();


        try {
            t.sendResponseHeaders(200, response.length());
            os.write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
