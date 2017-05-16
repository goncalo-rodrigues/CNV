package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ist.cnv.worker.AWSWorkerFactory;
import ist.cnv.worker.Worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RedirectRequest implements HttpHandler{
    private final List<Worker> workers;
    private Boolean isCreatingWorker =  false;
    private Worker bornWorker = null;
    private AWSWorkerFactory workerFactory;
    private static final int WORKTHREASHOLD = 5000000;//TODO put a nonRandom value
    private int unbornMachines = 0;

    public RedirectRequest(final List<Worker> workers){
        workerFactory = new AWSWorkerFactory();
        createNewWorker();
        this.workers = workers;
    }

    @Override
    public void handle(HttpExchange t) {
        // Decides which WebServer will handle the request

        int numMachines = workers.size();
        if(numMachines == 0) {
            do {
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

        Worker w = choseWorkerToRequest();
        System.out.println("R Going to: "+ w.getFullAddress());

        // Sends the request and waits for the result
        ContactChosenWSThread cct = new ContactChosenWSThread(t, w, this);
        Thread thread = new Thread(cct);
        thread.start();
    }

    private Worker choseWorkerToRequest(){
        System.out.println("->choseWorkerToRequest()");
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
            System.out.println(4);

            if (chosenWorker == null) {
                createNewWorker();
                chosenWorker = workers.get(0);//FIXME not sure if is the best choise;
            }
        }
        return chosenWorker;

    }

    private void createNewWorker(){
        if (unbornMachines == 0) {
            isCreatingWorker = true;
            bornWorker = workerFactory.createWorker();
            MetricPingThread pnt = new MetricPingThread(bornWorker, this);
//            PingNewbornThread pnt = new PingNewbornThread(bornWorker, this);
            Thread thread = new Thread(pnt);
            thread.start();
            unbornMachines++;
        }
    }

    private void updateWorkersState(){
    //TODO check if all still alive
        if(isCreatingWorker && bornWorker!=null) {
            if (workerFactory.isWorkerReady(bornWorker)) {
                workers.add(bornWorker);
                isCreatingWorker = false;
                bornWorker = null;
            }
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
            }
        }

        if (worker.getWorkload() == 0) {
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
            HeartbeatThread hbt = new HeartbeatThread(worker, this);
            Thread thread = new Thread(hbt);
            thread.start();
            unbornMachines--;
        }
    }

    private class WorkerComparator implements Comparator<Worker> {
        @Override
        public int compare(Worker o1, Worker o2) {
            return o1.getWorkload() - o2.getWorkload();
        }
    }
}

