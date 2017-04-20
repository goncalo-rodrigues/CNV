import com.sun.net.httpserver.HttpExchange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by goncalo on 20-04-2017.
 */
public class RequestImageThread implements Runnable {
    private HttpExchange t = null;

    public RequestImageThread(HttpExchange t) {
        this.t = t;
    }

    public void run() {
        try {
            String uri = t.getRequestURI().getRawPath();
            String[] uriSplitted = uri.split("/");
            if (uriSplitted.length < 3) {
                sendError("Invalid url");
                return;
            }
            String filename = uriSplitted[2];
            if (!filename.matches(".*\\.bmp")) {
                sendError("Invalid url");
                return;
            }





            byte[] image = extractBytes(filename);
            t.getResponseHeaders().set("Content-Type", "image/bmp");
            t.sendResponseHeaders(200, image.length);
            OutputStream os = t.getResponseBody();
            os.write(image);
            os.close();
        } catch(IOException e) {
            try {
                sendError("Image does not exist");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void sendError(String error)  throws  IOException{
        t.sendResponseHeaders(200, error.length());
        OutputStream os = t.getResponseBody();
        os.write(error.getBytes());
        os.close();
    }

    public byte[] extractBytes (String ImageName) throws IOException {
        // open image
        Path path = Paths.get(ImageName);
        if (Files.exists(path)) {
            byte[] data = Files.readAllBytes(path);
            return data;
        } else {
            throw new IOException("File does not exist");
        }

    }
}
