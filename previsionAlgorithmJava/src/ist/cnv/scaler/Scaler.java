package ist.cnv.scaler;

import ist.cnv.worker.AWSWorkerFactory;
import ist.cnv.worker.Worker;

import java.util.List;

public class Scaler implements Runnable {
    private static final int INTERVAL = 60000; // One minute
    private static final int MINUTES_TO_REDUCE = 5;
    private static final int MAX_LOAD_MACHINE = 5000000; //TODO: put a nonRandom value

    private final List<Worker> workers;
    private int timesBelow = 0;

    public Scaler(List<Worker> workers) {
        this.workers = workers;
    }

    @Override
    public void run() {
        System.out.println("Scaler: Up and running!");


        // It should work until the load balancer instance stops
        while(true) {
            try {
                long shareWork = 0;
                int nWorkers;
                long averageWork;
                Worker lowestMachine = null;

                Thread.sleep(INTERVAL);

                synchronized(workers) {
                    nWorkers = workers.size();

                    if(nWorkers > 0) {
                        lowestMachine = workers.get(0);
                        shareWork = lowestMachine.getWorkload();
                    }

                    for(int i = 1; i < nWorkers; i++) {
                        Worker currentMachine = workers.get(i);
                        if(currentMachine.getWorkload() < lowestMachine.getWorkload())
                            lowestMachine = currentMachine;

                        shareWork += currentMachine.getWorkload();
                    }
                }

                if(nWorkers > 0)
                    averageWork = shareWork / nWorkers;

                // TODO: Check if it needs to create a new worker when there is no available!!!
                else {
                    timesBelow = 0;
                    continue;
                }

                // TODO: Make this earlier then when it is needed
                if(averageWork > MAX_LOAD_MACHINE) {
                    System.out.println("I will need a new machine ASAP!");
                    timesBelow = 0;
                }

                else {
                    System.out.println("I think that I am good for now...");
                    timesBelow ++;
                }

                if(timesBelow == MINUTES_TO_REDUCE) {
                    timesBelow = 0;
                    System.out.println("It would be time to reduce a machine...");
                    AWSWorkerFactory factory = new AWSWorkerFactory();
                    factory.terminateWorker(lowestMachine);
                }

            } catch (InterruptedException e) {
                // Otherwise, just ignore it
                e.printStackTrace();
            }
        }
    }
}