package ist.cnv.worker;


public class Worker {
    private String id;
    private String address;
    public int workload = 0;

    public Worker(String workerID,String workerAddress){
        id = workerID;
        address = workerAddress;
    }

    public String getId(){ return id;}

    public String getAddress(){ return address;}

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
