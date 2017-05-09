package ist.cnv;

/**
 * Created by ant on 09-05-2017.
 */
public class File {
    String name;
    public int[][] cost;
    public int[][] area;
    DynamoDBConnection dynamoConnection;

    public File(String name,DynamoDBConnection dynamoConnection){
        this.name = name;
        this.dynamoConnection = dynamoConnection;
        //TODO check if already exists in DynamoDB
        cost = new int[40][40];
        area = new int[40][40];

        int total = 41*41;
        for(int x = 0; x <= 39; x++)
            for(int y = 0; y <= 39; y++)
                area[y][x]=total;
    }

    public void save(){
        dynamoConnection.saveImageData(name,cost,area);
    }


}
