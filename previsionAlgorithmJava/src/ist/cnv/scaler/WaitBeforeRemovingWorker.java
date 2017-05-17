package ist.cnv.scaler;

import ist.cnv.worker.AWSWorkerFactory;
import ist.cnv.worker.Worker;

/**
 * Created on 17-05-2017.
 */
public class WaitBeforeRemovingWorker implements Runnable {
    private Worker toTerminate;

    public WaitBeforeRemovingWorker(Worker toTerminate) {
        this.toTerminate = toTerminate;
    }

    @Override
    public void run() {
        while(toTerminate.getWorkload() != 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        AWSWorkerFactory factory = new AWSWorkerFactory();
        factory.terminateWorker(toTerminate);
    }
}
