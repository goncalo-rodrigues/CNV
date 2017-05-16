package ist.cnv.worker;


import java.net.MalformedURLException;
import java.net.URL;

public class Worker {
    private String id;
    private String address;
    private boolean deleted = false;

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
