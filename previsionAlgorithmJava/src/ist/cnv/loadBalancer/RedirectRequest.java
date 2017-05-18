package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ist.cnv.PrevisionAlgorithm;
import ist.cnv.scaler.Scaler;
import ist.cnv.worker.AWSWorkerFactory;
import ist.cnv.worker.Worker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class RedirectRequest implements HttpHandler{
    private final List<Worker> workers;
    private Boolean isCreatingWorker =  false;
    private AWSWorkerFactory workerFactory;
    private static final int WORKTHREASHOLD = 500000;//TODO put a nonRandom value

    private int unbornMachines = 0;
    private PrevisionAlgorithm oracle;

    public RedirectRequest(final List<Worker> workers){
        workerFactory = new AWSWorkerFactory();
        this.workers = workers;
        for(Worker w : workerFactory.getWorkersFromRunningInstances()) {
            PingNewbornThread hbt = new PingNewbornThread(w, this);
//            HeartbeatThread hbt = new HeartbeatThread(worker, this);
            Thread thread = new Thread(hbt);
            thread.start();
            unbornMachines++;
        }


        while (workers.size() + unbornMachines < 1)
            createNewWorker();

        ArrayList<String> imagesNames = new ArrayList<>();
        imagesNames.add("test01.txt");

        oracle = new PrevisionAlgorithm(imagesNames);
        oracle.addFile("test02.txt", 0.56942257);
        oracle.addFile("test03.txt", 0.74359062);
        oracle.addFile("test04.txt", 0.11292513);
        oracle.addFile("test05.txt", 0.21124866);
    }

    @Override
    public void handle(HttpExchange t) {
        // Decides which WebServer will handle the request
        Map<String, String> args = queryToMap(t.getRequestURI().getQuery());
        int sc,sr,wc,wr,coff,roff;
        String fname;
        long prevision =0;
        if(!(args.containsKey("f") && args.containsKey("sc") && args.containsKey("sr") && args.containsKey("wc") &&
                args.containsKey("wr") && args.containsKey("coff") && args.containsKey("roff"))) {
            handleError("Missing arguments", t);
            return;
        }

        fname = args.get("f");
        sc = Integer.parseInt(args.get("sc"));
        sr = Integer.parseInt(args.get("sr"));
        wc = Integer.parseInt(args.get("wc"));
        wr = Integer.parseInt(args.get("wr"));
        coff = Integer.parseInt(args.get("coff"));
        roff = Integer.parseInt(args.get("roff"));
        boolean validated = false;
        if(wc > sc) {
            handleError("wc>sc", t);
        } else if(wr > sr) {
            handleError("wr>sr", t);
        } else if(coff > sc - wc) {
            handleError("coff>(sc-wc)", t);
        } else if(roff > sr - wr) {
            handleError("roff>(sr-wr)", t);
        } else {
            validated = true;
        }
        if (!validated) return;

        prevision = computePrevision(fname, sc,sr,wc,wr,coff,roff);

        int numMachines = workers.size();

        if(numMachines == 0) {
            do {
                System.out.println("Waiting for some machine...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (workers) {
                    numMachines = workers.size();
                }

            } while (numMachines == 0);
        }

        Worker w = null;
        while((w = choseWorkerToRequest()) == null) {
            System.out.println("Super loaded!!, waiting");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("R Going to: "+ w.getFullAddress());

        // Sends the request and waits for the result
        ContactChosenWSThread cct = new ContactChosenWSThread(t, w, this, prevision);
        cct.setParameters(fname,sc,sr,wc,wr,coff,roff);
        cct.start();
    }

    private void handleError(String s, HttpExchange t) {
        String response = s + "\n" + "Please try again";
        try {
            OutputStream os = t.getResponseBody();
            t.sendResponseHeaders(200, response.length());
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long computePrevision(String f, int sc, int sr, int wc, int wr, int coff, int roff) {
        return oracle.estimateCost(f, sc, sr, wc, wr, coff, roff);
    }

    public void update(String f, int sc, int sr, int wc, int wr, int coff, int roff, long cost) {
        oracle.insertData(f, sc, sr, wc, wr, coff, roff, cost);
    }

    private Worker choseWorkerToRequest(){

        Worker chosenWorker = null;
        synchronized (workers) {
            //In case that we dont have any worker ready
//            updateWorkersState();
            Collections.sort(workers, new WorkerComparator());
            for (Worker w : workers)
                if (w.getWorkload() < WORKTHREASHOLD) {
                    chosenWorker = w;
                    break;
                }

            if (chosenWorker == null) {

                chosenWorker = workers.get(0);
                if (chosenWorker.getWorkload() > Scaler.MAX_LOAD_MACHINE) {
                    return null;
                } else {
                    System.out.println("->choseWorkerToRequest() with load " +  chosenWorker.getWorkload() + "against" + workers.get(workers.size()-1).getWorkload());
                }
            }
        }
        return chosenWorker;

    }

    public void createNewWorker(){
        if (unbornMachines == 0) {
            isCreatingWorker = true;
            Worker bornWorker = workerFactory.createWorker();
            PingNewbornThread pnt = new PingNewbornThread(bornWorker, this);
            Thread thread = new Thread(pnt);
            thread.start();
            unbornMachines++;
        }
    }


    public void removeWorker(Worker worker) {
        boolean noWorkers = false;
        synchronized (workers) {
            if (workers.contains(worker)) {
                workers.remove(worker);
                System.out.println("Removed worker " + worker.getAddress());
                if (workers.size() + unbornMachines <= 0) {
                    System.out.println("WARNING!! NO MACHINES AVAILABLE! Creating new");
                    noWorkers = true;
                }
                worker.interruptAllRequests();
            }
        }

        terminateWorker(worker);
        if (noWorkers) {
            createNewWorker();
        }
    }

    public void killWorker(Worker worker) {
        synchronized (workers) {
            if (workers.contains(worker)) {
                workers.remove(worker);
                worker.delete();
                System.out.println("KILLED worker " + worker.getId());
            }
        }

        if (worker.isEmpty()) {
            terminateWorker(worker);
        }
    }

    public void terminateWorker(Worker worker) {
        workerFactory.terminateWorker(worker);
    }

    public void spawnWorker(Worker worker) {
        synchronized (workers) {
            System.out.println("Worker came to life " + worker.getAddress());
            workers.add(worker);
            MetricPingThread hbt = new MetricPingThread(worker, this);
            Thread thread = new Thread(hbt);
            thread.start();
            unbornMachines--;
        }
    }

    private class WorkerComparator implements Comparator<Worker> {
        @Override
        public int compare(Worker o1, Worker o2) {
            long w1 = o1.getWorkload();
            long w2 = o2.getWorkload();
            if (w1 > w2) return 1;
            if (w1 == w2) return 0;
            return -1;
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
}

