package ist.cnv.scaler;

import ist.cnv.loadBalancer.RedirectRequest;
import ist.cnv.worker.AWSWorkerFactory;
import ist.cnv.worker.Worker;

/**
 * Created on 17-05-2017.
 */
public class WaitBeforeRemovingWorkerThread implements Runnable {
    private Worker toTerminate;
    private RedirectRequest rr;

    public WaitBeforeRemovingWorkerThread(Worker toTerminate, RedirectRequest rr) {
        this.toTerminate = toTerminate;
        this.rr = rr;
    }

    @Override
    public void run() {
        try {
            rr.killWorker(toTerminate);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
