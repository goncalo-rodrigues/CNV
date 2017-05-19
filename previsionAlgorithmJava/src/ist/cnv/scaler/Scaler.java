package ist.cnv.scaler;

import ist.cnv.loadBalancer.RedirectRequest;
import ist.cnv.worker.Worker;

import java.util.List;

public class Scaler implements Runnable {
    private static final int INTERVAL = 60000; // One minute
//    private static final int INTERVAL = 5000;

    // TODO: Increase the number to 5 minutes
    private static final int MINUTES_TO_REDUCE = 5;
    public static final int MAX_LOAD_MACHINE = 50000000; //TODO: put a nonRandom value
    public static final double MAX_LOAD_MACHINE_SEC = 30;
    private static final float INCREASE_THRESHOLD = (float) 0.8;
    private static final int MAX_NR_MACHINES = 30;
    private static final float DECREASE_THRESHOLD = (float) 0.5;
    private static final double DECAY = 0.2;

    private final List<Worker> workers;
    private final RedirectRequest loadBalancer;
    private int timesBelow = 0;

    public Scaler(List<Worker> workers, RedirectRequest loadBalancer) {
        this.workers = workers;
        this.loadBalancer = loadBalancer;
    }

    @Override
    public void run() {
        System.out.println("Scaler: Up and running!");

        long shareWork = 0;
        long workFlow = 0;

        // It should work until the load balancer instance stops
        while(true) {
            try {
                int nWorkers;
                long averageWork;
                long averageFlow;
                Worker lowestMachine = null;

                Thread.sleep(INTERVAL);

                shareWork *= DECAY;
                workFlow *= DECAY;
                synchronized(workers) {
                    nWorkers = workers.size() + loadBalancer.unbornMachines;

                    if(workers.size() > 0) {
                        lowestMachine = workers.get(0);
                    }

                    for(Worker currentMachine: workers) {
                        if(currentMachine.getWorkload() < lowestMachine.getWorkload())
                            lowestMachine = currentMachine;

                        shareWork += (1-DECAY) * Math.min(MAX_LOAD_MACHINE, currentMachine.getWorkload());
                    }


                    shareWork += (1-DECAY) * loadBalancer.getPendingLoad();

                    workFlow += loadBalancer.getReceivedLoad()*1000*(1-DECAY)*MAX_LOAD_MACHINE_SEC / INTERVAL;
                    loadBalancer.resetReceivedLoad();

                }
                System.out.println("sharework computed " + shareWork);
                System.out.println("flow computed " + workFlow);
                if(nWorkers > 0) {
                    averageWork = shareWork / nWorkers;
                    averageFlow = workFlow / nWorkers;
                    System.out.println("AVERAGE WORK: " + averageWork);
                    System.out.println("AVERAGE FLOW: " +  averageFlow);
                }


                // TODO: Check if it needs to create a new worker when there is no available!!!
                else {
                    timesBelow = 0;
                    continue;
                }

                // TODO: Make this earlier then when it is needed
                if(averageWork > MAX_LOAD_MACHINE * INCREASE_THRESHOLD) {
                    System.out.println("I will need a new machine ASAP!");
                    timesBelow = 0;
                    int machinesNeeded = Math.max(1,(int) loadBalancer.getPendingLoad() / MAX_LOAD_MACHINE - loadBalancer.unbornMachines);
                    for (int i=0; i < machinesNeeded; i++)
                        if(nWorkers++ < MAX_NR_MACHINES) {
                            increaseNrMachines();
                        }

                } else if (averageFlow < MAX_LOAD_MACHINE * DECREASE_THRESHOLD) {
                    System.out.println("I think that I am good for now...");
                    timesBelow ++;
                }
//                } else if (lowestMachine.getWorkload() < MAX_LOAD_MACHINE * DECREASE_THRESHOLD) {
//                    System.out.println("I think that I am good for now...");
//                    timesBelow ++;
//                }
                else {
                    timesBelow = 0;
                }

                if(timesBelow == MINUTES_TO_REDUCE) {
                    timesBelow--;
                    System.out.println("It would be time to reduce a machine...");

                    if(nWorkers == 1) {
                        System.out.println("...but I have only one. So...");
                        continue;
                    }

                    nWorkers --;
                    averageWork = (shareWork - lowestMachine.getWorkload()) / nWorkers;

                    if(averageWork > MAX_LOAD_MACHINE * INCREASE_THRESHOLD) {
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
        WaitBeforeRemovingWorkerThread wbrw = new WaitBeforeRemovingWorkerThread(lowest, loadBalancer);
        new Thread(wbrw).start();
    }

    private void increaseNrMachines() {
        loadBalancer.createNewWorker();
    }
}