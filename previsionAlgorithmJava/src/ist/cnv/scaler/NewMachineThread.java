package ist.cnv.scaler;

import ist.cnv.worker.AWSWorkerFactory;
import ist.cnv.worker.Worker;

import java.util.List;

/**
 * Created on 17-05-2017.
 */
public class NewMachineThread implements Runnable {
    private List<Worker> workers;

    public NewMachineThread(List<Worker> workers) {
        this.workers = workers;
    }

    @Override
    public void run() {
        AWSWorkerFactory factory = new AWSWorkerFactory();
        Worker toAdd = factory.createWorker();
        while(!factory.isWorkerReady(toAdd)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized(workers) {
            workers.add(toAdd);
        }
    }
}
