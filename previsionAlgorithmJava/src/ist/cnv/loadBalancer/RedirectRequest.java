package ist.cnv.loadBalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ist.cnv.worker.AWSWorkerFactory;
import ist.cnv.worker.Worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RedirectRequest implements HttpHandler{
    private List<Worker> workers = new ArrayList<Worker>();
    private Boolean isCreatingWorker =  false;
    private Worker bornWorker = null;
    private AWSWorkerFactory workerFactory;
    private Object chosingWorkerLock = new Object();
    private static final int WORKTHREASHOLD = 5000000;//TODO put a nonRandom value

    public RedirectRequest(){
        workerFactory = new AWSWorkerFactory();
        createNewWorker();
    }

    @Override
    public void handle(HttpExchange t) {
        // Decides which WebServer will handle the request
        if(workers.size() == 0) {
            while (!workerFactory.isWorkerReady(bornWorker)) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            workers.add(bornWorker);
            bornWorker = null;
        }

        String url = "http://" + choseWorkerToRequest();
        System.out.println("R Going to: "+ url);

        // Sends the request and waits for the result
        ContactChosenWSThread cct = new ContactChosenWSThread(t, url);
        Thread thread = new Thread(cct);
        thread.start();
    }

    private String choseWorkerToRequest(){
        System.out.println("->choseWorkerToRequest()");
        Worker chosenWorker = null;
        synchronized (chosingWorkerLock) {
            //In case that we dont have any worker ready
            updateWorkersState();
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
        return chosenWorker.getAddress();

    }

    private void createNewWorker(){
        if (!isCreatingWorker) {
            isCreatingWorker = true;
            bornWorker = workerFactory.createWorker();
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

    private class WorkerComparator implements Comparator<Worker> {
        @Override
        public int compare(Worker o1, Worker o2) {
            return o1.getWorkload() - o2.getWorkload();
        }
    }
}

