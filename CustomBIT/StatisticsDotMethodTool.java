import BIT.highBIT.*;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by goncalo on 14-04-2017.
 */
public class StatisticsDotMethodTool {
    private static PrintStream out = null;
//    private static HashMap<Long, Long> metricMap = new HashMap<Long, Long>();
    private static int max_threads = 1000;
    private static long metric[] = new long[max_threads];
    private static long times[] = new long[max_threads];
    private static Lock locks[] = new Lock[max_threads];

    static {
        for (int i=0; i < max_threads; i++) locks[i] = new ReentrantLock();
    }

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();

        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
                // create class info object
                ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);

                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    if (routine.getMethodName().equals("dot")) {
                        routine.addBefore("StatisticsDotMethodTool", "mcount", new Integer(1));
                    }

                    if (routine.getMethodName().equals("main")) {
                        routine.addBefore("StatisticsDotMethodTool", "resetcount", 0);
                    }

                }
                ci.addAfter("StatisticsDotMethodTool", "printICount", ci.getClassName());
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }

    public static void printICount(String foo) {

        int threadId = (int) Thread.currentThread().getId();
        int remainder = threadId % metric.length;

        times[remainder] = System.nanoTime() - times[remainder];
        System.out.println("method dot was executed " + metric[remainder] + " times and took " +
                + (times[remainder])*1e-9 + "seconds");


    }

    public static void resetcount(int ignored) {
        int threadId = (int) Thread.currentThread().getId();
        int id = threadId %  max_threads;
        locks[id].lock();
        metric[id]=0;
        times[id] = System.nanoTime();
    }

    public static void mcount(int incr) {

        int threadId = (int) Thread.currentThread().getId();
        metric[threadId % max_threads]++;
    }

    public static long getMetric() {
        int threadId = (int) Thread.currentThread().getId();
        int id = threadId % max_threads;
        long val = metric[id];
        locks[id].unlock();
        return val;
    }

    public static long getTime() {
        int threadId = (int) Thread.currentThread().getId();
        return times[threadId %  max_threads];
    }
}
