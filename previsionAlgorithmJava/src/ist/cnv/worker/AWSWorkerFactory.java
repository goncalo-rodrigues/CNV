package ist.cnv.worker;


import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;

public class AWSWorkerFactory {
    public static final String IMAGEID = "ami-008ceb60"; //TODO replace this with our image name
    public static final String INSTANCETYPE = "t2.micro";
    public static final String SECURITYGROUP = "all";//"default"; //TODO replace this , this was in sdk sample code


    private AmazonEC2 amazonEC2 ;
    private RunInstancesRequest runInstanceRequest = null;

    public AWSWorkerFactory(){
        AWSCredentials credentials;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            System.out.println("Failed to load Credentials");
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);

        }
        amazonEC2 = new AmazonEC2Client(credentials);
        Region region = Region.getRegion(Regions.US_WEST_2);
        amazonEC2.setRegion(region);

        //we create 1 request that will e always the same
        runInstanceRequest = new RunInstancesRequest();
        runInstanceRequest.withImageId(IMAGEID)
                .withInstanceType(INSTANCETYPE)
                .withSecurityGroups(SECURITYGROUP)
                .withMaxCount(1)
                .withMinCount(1);
    }

    public Worker createWorker(){
        if (amazonEC2==null){
            System.out.println("ec3 == null");
        }
        RunInstancesResult  result = amazonEC2.runInstances(runInstanceRequest);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String id = result.getReservation().getInstances().get(0).getInstanceId();
        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(id);
        DescribeInstancesResult describeInstanceResult = amazonEC2.describeInstances(describeInstanceRequest);
        String address = describeInstanceResult.getReservations().get(0).getInstances().get(0).getPublicDnsName();//TODO check if get ip is best
        return new Worker(id,address);
    }

    // FIXME: Terminated instances appear as ready to work!!!
    public ArrayList<Worker> getWorkersFromRunningInstances(){
        DescribeInstanceStatusRequest statusRequest = new DescribeInstanceStatusRequest().withIncludeAllInstances(true);
        DescribeInstanceStatusResult result = amazonEC2.describeInstanceStatus(statusRequest);
        ArrayList<String> okIds = new ArrayList<>();
        ArrayList<Worker> workers = new ArrayList<>();

        if(result.getInstanceStatuses()!=null && result.getInstanceStatuses().size()!=0)
            for (InstanceStatus instance : result.getInstanceStatuses())
                if(instance.getInstanceStatus().getStatus().equals("ok"))
                    okIds.add(instance.getInstanceId());

        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(okIds);
        DescribeInstancesResult describeInstanceResult = amazonEC2.describeInstances(describeInstanceRequest);
        for(Reservation r: describeInstanceResult.getReservations()){
            workers.add(new Worker(r.getInstances().get(0).getInstanceId(),r.getInstances().get(0).getPublicDnsName()));
        }

        System.out.println("Got already on workers:");
        for(Worker worker: workers){
            System.out.println(worker);
        }

        return  workers;
    }



    public void terminateWorker(Worker worker){
        String instanceID = worker.getId();
        TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest();
        terminateRequest.withInstanceIds(instanceID);
        amazonEC2.terminateInstances(terminateRequest);
    }

    public void terminateAllWorkers(){
        DescribeInstanceStatusRequest statusRequest = new DescribeInstanceStatusRequest().withIncludeAllInstances(true);
        DescribeInstanceStatusResult result = amazonEC2.describeInstanceStatus(statusRequest);
        List<String> workerIds = new ArrayList<>();

        if(result.getInstanceStatuses()!=null && result.getInstanceStatuses().size()!=0) {
            for (InstanceStatus instance : result.getInstanceStatuses()) {
                workerIds.add(instance.getInstanceId());
            }
        }
        if(workerIds.size()>0){
            TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest();
            terminateRequest.withInstanceIds(workerIds);
            amazonEC2.terminateInstances(terminateRequest);
        }
    }




    public boolean isWorkerReady(Worker worker){
        String instanceID = worker.getId();
        DescribeInstanceStatusRequest statusRequest = new DescribeInstanceStatusRequest().withInstanceIds(instanceID);
        DescribeInstanceStatusResult result = amazonEC2.describeInstanceStatus(statusRequest);

        if(result.getInstanceStatuses()!=null && result.getInstanceStatuses().size()!=0) {
            String status = result.getInstanceStatuses().get(0).getInstanceStatus().getStatus();
            System.out.println(status);
            if (status.equals("ok"))
                return true;
        }
        return false;
    }//result.instanceStatuses[0].instanceStatus.Status  "ok" se estiver pronta

    public static void main(String[] args){
        AWSWorkerFactory factory = new AWSWorkerFactory();
        /*Worker worker1 = factory.createWorker();
        factory.createWorker();
        System.out.println("CREATED  instanceid: "+worker1.getId()+" address:"+worker1.getAddress());
        while(!factory.isWorkerReady(worker1)) {
            System.out.println("instanceid = "+worker1.getId()+" NOT READY");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        ArrayList<Worker> workers =  factory.getWorkersFromRunningInstances();
        for(Worker worker: workers){
            System.out.println(worker);
        }
        //factory.terminateAllWorkers();
        System.out.println("finished");
    }

}
