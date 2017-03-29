import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import raytracer.Main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
			String query = t.getRequestURI().getQuery();
			Map<String, String> args = queryToMap(query);
			String response = "###\n";
            for (Map.Entry e : args.entrySet()) {
				response += e.getKey() + "\t" + e.getValue() + "\n";
			}
			response += "###";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

	private static Map<String, String> queryToMap(String query){

	    Map<String, String> result = new HashMap<String, String>();
		if (query == null || query.length() == 0) {
			return result;	
		}
	    for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length>1) {
				result.put(pair[0], pair[1]);
			}else{
				result.put(pair[0], "");
			}
	    }
	    return result;
	}

}
