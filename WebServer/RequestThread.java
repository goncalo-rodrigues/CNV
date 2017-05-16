import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import com.sun.net.httpserver.HttpExchange;

import raytracer.Main;

public class RequestThread implements Runnable {
  private static String statsFilename = "stats.txt";
  private static boolean debug = false;
  private HttpExchange t = null;
  private static ArrayList<String> keys;

  public RequestThread(HttpExchange t) {
    this.t = t;
  }

  public void run() {
      int sc=0, sr=0, wc=0, wr=0, coff=0, roff = 0;
      long metric = 0;
    try {
      String query = t.getRequestURI().getQuery();
      Map<String, String> args = queryToMap(query);
      String response = "";

//      response += "<!doctype html><head></head><body>";
//      response = String.valueOf(Thread.currentThread().getId());
      try {
        if(args.containsKey("f") && args.containsKey("sc") && args.containsKey("sr") && args.containsKey("wc") &&
                args.containsKey("wr") && args.containsKey("coff") && args.containsKey("roff")) {

          boolean cont = true;

          sc = Integer.parseInt(args.get("sc"));
          sr = Integer.parseInt(args.get("sr"));
          wc = Integer.parseInt(args.get("wc"));
          wr = Integer.parseInt(args.get("wr"));
          coff = Integer.parseInt(args.get("coff"));
          roff = Integer.parseInt(args.get("roff"));

          if(wc > sc) {
            response += "\nwc > sc. Please try again.";
            cont = false;
          } else if(wr > sr) {
            response += "\nwr > sr. Please try again.";
            cont = false;
          } else if(coff > sc - wc) {
            response += "\ncoff > sc - wc. Please try again.";
            cont = false;
          } else if(roff > sr - wr) {
            response += "\nroff > sr - wr. Please try again.";
            cont = false;
          }

          if(cont) {
            checkRequestedFile(args.get("f"));
            String outName = Thread.currentThread().getId() + ".bmp";
            String[] args_rt = {args.get("f"), outName,
                    args.get("sc"), args.get("sr"), args.get("wc"), args.get("wr"), args.get("coff"), args.get("roff")};

            raytracer.Main.main(args_rt);
            metric = StatisticsDotMethodTool.getMetric();
            response += String.valueOf(metric) + "\n";
            response += "images/" + outName + "\n";
//            response += "<br> Metric:" + metric + "<br>";
//            response += "Time taken:" + StatisticsDotMethodTool.getTime()*1e-9 + "<br>";
//            response += "<br> <a href=\"images/"+ outName + "\">See image here</a>";
          }
        }

        else
          response += "\nThere is an argument missing from the request. Please try again.";
      } catch (InterruptedException e) {
        // Ignoring...
      } catch (FileNotFoundException e) {
        response += "\nFile was not found. Please try again.";
      }

//      response += "</body></html>";
      
      t.getResponseHeaders().set("Content-type", "text/plain");
      t.sendResponseHeaders(200, response.length());

      boolean all_metrics = false;
      if (debug && !all_metrics) {
        try {
          File f = new File(statsFilename);
          if(!f.exists() && !f.isDirectory())
            {
                    f.createNewFile();
                    Files.write(Paths.get(statsFilename), "sc,sr,wc,wr,coff,roff,metric,time\n".getBytes(),
                            StandardOpenOption.WRITE);
            }
          Files.write(Paths.get(statsFilename),
                  String.format(Locale.US, "%d,%d,%d,%d,%d,%d,%d,%f\n",
                          sc, sr, wc, wr, coff, roff,
                          metric,
                          StatisticsDotMethodTool.getTime()*1e-9).getBytes(),
                  StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
      }
      if (debug && all_metrics) {

          try {
              File f = new File(statsFilename);
              HashMap<String, BigInteger> methodMap = StatisticsToolToFile.getMethodInstrMap();
              if((!f.exists() && !f.isDirectory()) || keys==null)
              {
                  f.createNewFile();
                  keys = new ArrayList<>(methodMap.keySet());
                  String columnNames = "sc,sr,wc,wr,coff,roff,size,instrs,bb";
                  for (String key: keys) {
                      columnNames += ","+key;
                  }
                  columnNames += "\n";
                  Files.write(Paths.get(statsFilename), columnNames.getBytes(),
                          StandardOpenOption.WRITE);
              }
              String columnVals = String.format(Locale.US, "%d,%d,%d,%d,%d,%d,%d,%d,%d",
                      sc,sr,wc,wr,coff,roff,wc*wr,StatisticsToolToFile.getInstrs(),StatisticsToolToFile.getBBcount());
              for (String key: keys) {
                  columnVals += ","+ (methodMap.containsKey(key) ? methodMap.get(key).toString() : "0");
              }
              columnVals += "\n";
              Files.write(Paths.get(statsFilename),columnVals.getBytes(),
                      StandardOpenOption.APPEND);
          }catch (IOException e) {
              //exception handling left as an exercise for the reader
          }

      }
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();

    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  private Map<String, String> queryToMap(String query) {
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

  private void checkRequestedFile(String name) throws IOException {
    Path dir = Paths.get(".");

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.txt")) {
      boolean found = false;
      String fileName = "./" + name;

      for (Path file : stream) {
        if(fileName.equals(file.toString())) {
          found = true;
          break;
        }
      }

      if(!found)
        throw new FileNotFoundException();
    }
  }
}
