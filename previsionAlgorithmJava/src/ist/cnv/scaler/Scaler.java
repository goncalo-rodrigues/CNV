package ist.cnv.scaler;

import ist.cnv.worker.Worker;

import java.util.List;

public class Scaler implements Runnable {
    private static final int INTERVAL = 60000; // One minute

    // TODO: Increase the number to 5 minutes
    private static final int MINUTES_TO_REDUCE = 1;
    private static final int MAX_LOAD_MACHINE = 5000000; //TODO: put a nonRandom value
    private static final float INCREASE_THRESHOLD = (float) 0.8;

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
                if(averageWork > MAX_LOAD_MACHINE * INCREASE_THRESHOLD) {
                    System.out.println("I will need a new machine ASAP!");
                    timesBelow = 0;

                    increaseNrMachines();
                }

                else {
                    System.out.println("I think that I am good for now...");
                    timesBelow ++;
                }

                if(timesBelow == MINUTES_TO_REDUCE) {
                    timesBelow = 0;
                    System.out.println("It would be time to reduce a machine...");

                    if(nWorkers == 1) {
                        System.out.println("... but I have only one. So ...");
                        continue;
                    }

                    nWorkers --;
                    averageWork = shareWork / nWorkers;

                    if(averageWork > MAX_LOAD_MACHINE) {
                        System.out.println("If I take one, the system will be overloaded...");
                        continue;
                    }

                    // It will wait the work termination before removing the machine
                    terminateWorkerSafe(lowestMachine);
                }

            } catch (InterruptedException e) {
                // Otherwise, just ignore it
                e.printStackTrace();
            }
        }
    }

    private void terminateWorkerSafe(Worker lowest) {
        synchronized(workers) {
            workers.remove(lowest);
        }

        WaitBeforeRemovingWorkerThread wbrw = new WaitBeforeRemovingWorkerThread(lowest);
        new Thread(wbrw).start();
    }

    private void increaseNrMachines() {
        NewMachineThread nmt = new NewMachineThread(workers);
        new Thread(nmt).start();
    }
}