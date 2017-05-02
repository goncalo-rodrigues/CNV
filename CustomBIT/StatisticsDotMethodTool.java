import BIT.highBIT.BasicBlock;
import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

import java.io.*;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by goncalo on 14-04-2017.
 */
public class StatisticsDotMethodTool {
    private static PrintStream out = null;
    private static ConcurrentHashMap<Long, Long> metricMap = new ConcurrentHashMap<>();

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
                }
                ci.addAfter("StatisticsDotMethodTool", "printICount", ci.getClassName());
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }

    public static void printICount(String foo) {

        long threadId = Thread.currentThread().getId();

        try {
            PrintWriter writer = new PrintWriter("dynamic_" + threadId + ".txt", "UTF-8");
            writer.println(String.valueOf(m_count.get()));
            writer.flush();
            writer.close();
            System.out.println("method dot was executed " + m_count + " times.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    public static void mcount(int incr) {
        Long threadId = new Long(Thread.currentThread().getId());
        Long metric = metricMap.get(threadId);
        metricMap.put(threadId, metric == null? 0 : metric + incr);
    }
}
