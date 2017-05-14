package ist.cnv.worker;


public class Worker {
    private String id;
    private String address;

    public Worker(String workerID,String workerAddress){
        id = workerID;
        address = workerAddress;
    }

    public String getId(){ return id;}

    public String getAddress(){ return address;}
}
