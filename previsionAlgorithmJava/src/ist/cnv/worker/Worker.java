package ist.cnv.worker;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Worker {
    private String id;
    private String address;
    private boolean deleted = false;

    public HashMap<String, Long> workloads = new HashMap<>();
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
        workloads.put(rid, prevision);
        workload+=prevision;
    }

    public void removeRequest(String rid) {
        workload-=workloads.get(rid);
        workloads.remove(rid);
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
