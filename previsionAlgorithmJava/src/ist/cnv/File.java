package ist.cnv;

/**
 * Created by ant on 09-05-2017.
 */
public class File {
    String name;
    public int[][] cost;
    public int[][] area;
    DynamoDBConnection dynamoConnection;
    static int TSIZE = 40;

    public File(String name,DynamoDBConnection dynamoConnection){
        this.name = name;
        this.dynamoConnection = dynamoConnection;
        //TODO check if already exists in DynamoDB
        load();
    }

    public void load(){
        String result[] = dynamoConnection.getImageData(this.name);
        if (result != null) {
            cost = stringToIntList(result[0]);
            area = stringToIntList(result[1]);
        }
        else{
            cost = new int[TSIZE][TSIZE];
            area = new int[TSIZE][TSIZE];

            int total = (TSIZE+1)*(TSIZE+1);
            for(int x = 0; x < TSIZE; x++)
                for(int y = 0; y < TSIZE ; y++)
                    area[y][x]=total;
        }
    }

    public void save(){
        String costString = intListToString(cost);
        String areaString = intListToString(area);
        dynamoConnection.saveImageData(name,costString,areaString);
    }

    private String intListToString(int[][] intArray){
        String output = "";
        for(int x = 0; x < TSIZE; x++)
            for(int y = 0; y < TSIZE ; y++)
                output += intArray[y][x]+";";
        return output;
    }

    private int[][] stringToIntList(String arrayString){
        int[][] array = new int[TSIZE][TSIZE];
        int i = 0;
        String[] list = arrayString.split(";");
        for(int x = 0; x < TSIZE; x++)
            for(int y = 0; y < TSIZE ; y++){
                array[y][x]= Integer.parseInt(list[i]);
                i++;
            }
        return array;
    }


}
