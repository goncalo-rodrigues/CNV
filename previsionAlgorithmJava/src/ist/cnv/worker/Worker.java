package ist.cnv.worker;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Worker {
    private String id;
    private String address;
    private boolean deleted = false;

    private final Object lock = new Object();
    private HashMap<String, Long> workloads = new HashMap<>();
    private HashMap<String, Long> previsions = new HashMap<>();
    public int workload = 0;

    public Worker(String workerID,String workerAddress){
        id = workerID;
        address = workerAddress;
    }

    public String getId(){ return id;}

    public void delete() {
        deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void addRequest(String rid, long prevision) {
        synchronized (lock) {
            previsions.put(rid, prevision);
            workloads.put(rid, prevision);
            workload+=prevision;
        }
        System.out.println("Workload of " + id + " updated to " + workload);
    }

    public void removeRequest(String rid) {
        synchronized (lock) {
            workload-=workloads.get(rid);
            previsions.remove(rid);
        }
        System.out.println("Workload of " + id + " updated to " + workload);
    }

    public void updateRequest(String rid, long metricSoFar) {
        synchronized (lock) {
            long w = previsions.get(rid);
            long p = w - metricSoFar;
            long oldw = workloads.get(rid);
            long neww = Math.max(0, p);
            workloads.put(rid, neww);
            workload += (neww-oldw);
        }
        System.out.println("Workload of " + id + " updated to " + workload);
    }

    public String getAddress(){ return address;}

    public String getFullAddress() {
        String res = null;

        try {
            res = new URL(new URL("http://" + address), "r.html").toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public int getWorkload(){
        return workload;
    }

    public void setWorkload(int workload){
        this.workload = workload;
    }

    public String toString() {
        return "id: " + id + ", address: " + address + ", workload: " + workload ;
    }
}
