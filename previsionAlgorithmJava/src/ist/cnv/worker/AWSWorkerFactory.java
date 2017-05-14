package ist.cnv.worker;


import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class AWSWorkerFactory {
    public static final String IMAGEID = "ami-8c1fece5"; //TODO replace this with our image name
    public static final String INSTANCETYPE = "t1.micro";
    public static final String SECURITYGROUP = "GettingStartedGroup"; //TODO replace this , this was in sdk sample code


    private AmazonEC2 ec2 = null;
    private RunInstancesRequest runInstanceRequest = null;

    public AWSWorkerFactory(){
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        AmazonEC2 ec2 = new AmazonEC2Client(credentials);
        Region region = Region.getRegion(Regions.US_WEST_2);
        ec2.setRegion(region);

        //we create 1 request that will e always the same
        runInstanceRequest = new RunInstancesRequest();
        runInstanceRequest.withImageId(IMAGEID)
                .withInstanceType(INSTANCETYPE)
                .withSecurityGroups(SECURITYGROUP)
                .withMaxCount(1)
                .withMinCount(1);
    }

    public Worker createWorker(){
        RunInstancesResult  result = ec2.runInstances(runInstanceRequest);
        String id = result.getReservation().getInstances().get(0).getInstanceId();
        String address = result.getReservation().getInstances().get(0).getPublicDnsName();//TODO check if get ip is best
        return new Worker(id,address);
    }

    public void terminateWorker(Worker worker){
        String instanceID = worker.getId();
        TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest();
        terminateRequest.withInstanceIds(instanceID);
        ec2.terminateInstances(terminateRequest);
    }



    public static void main(String[] args){

        System.out.println(Regions.US_WEST_2);
    }

}
