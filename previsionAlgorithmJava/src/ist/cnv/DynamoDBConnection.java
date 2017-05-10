package ist.cnv;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.GetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ant on 09-05-2017.
 */
public class DynamoDBConnection {
    private AmazonDynamoDBClient dynamoDB;
    private DynamoDB dynamo;
    private static String TABLE_NAME = "images";
    private static String TABLE_KEY_NAME = "name";

    public DynamoDBConnection(){
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
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
        dynamo = new DynamoDB(dynamoDB);

        try {

            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(TABLE_NAME)
                    .withKeySchema(new KeySchemaElement().withAttributeName(TABLE_KEY_NAME).withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName(TABLE_KEY_NAME).withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(5L));//TODO fix values

            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(dynamoDB, TABLE_NAME);
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void saveImageData(String name, String cost, String area){
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();

        item.put(TABLE_KEY_NAME, new AttributeValue(name));
        item.put("cost",new AttributeValue(cost));
        item.put("area",new AttributeValue(area));
        System.out.println(item);
        System.out.println(item.keySet());
        
        PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME,item);
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        System.out.println("Result: " + putItemResult);

        /*Table table = dynamo.getTable(TABLE_NAME);
        Item item = new Item().withString("name", name).withList("cost",cost).withList("area",area);
        item.withBinarySet("cost",cost);
        PutItemOutcome result = table.putItem(item);
        System.out.println(result);*/

    }


    public String[] getImageData(String name){
        Table table = dynamo.getTable(TABLE_NAME);
        GetItemOutcome outcome = table.getItemOutcome(TABLE_KEY_NAME, name);
        Item item = outcome.getItem();
        if(item == null)
            return null;
        String output[]= {item.getString("cost"),item.getString("area")};
        return output;
    }

}
