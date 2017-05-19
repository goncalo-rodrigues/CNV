package ist.cnv;

/**
 * Created by ant on 09-05-2017.
 */
public class File {
    private static int TSIZE = 40;
    private static int SAVESUNTILSEND = 15;
    private static int STARTINGCOST = 11; //TODO refine this number see all the images result and get a good etimation
    String name;
    public int[][] cost;
    public int[][] area;
    public double normalizingConstant;
    DynamoDBConnection dynamoConnection;

    private int acumulatedSaves = 0;


    public File(String name,double normalizingConstant ,DynamoDBConnection dynamoConnection){
        this.name = name;
        this.normalizingConstant = normalizingConstant;
        this.dynamoConnection = dynamoConnection;
        //TODO check if already exists in DynamoDB
        load();
    }

    public void load(){
        String result[] = dynamoConnection.getImageData(this.name);
        if (result != null) {
            cost = stringToIntList(result[0]);
            area = stringToIntList(result[1]);
            normalizingConstant = Double.parseDouble(result[2]);
        }
        else{
            cost = new int[TSIZE][TSIZE];
            area = new int[TSIZE][TSIZE];

            int total = (TSIZE+1)*(TSIZE+1);
            for(int x = 0; x < TSIZE; x++)
                for(int y = 0; y < TSIZE ; y++) {
                    area[y][x] = total;
                    cost[y][x] = STARTINGCOST;
                }
        }
    }

    public void save(){
        acumulatedSaves ++;
        if (acumulatedSaves >= SAVESUNTILSEND) {
            forceSave();
            acumulatedSaves = 0;
        }
    }

    public void forceSave(){
        String costString = intListToString(cost);
        String areaString = intListToString(area);
        dynamoConnection.saveImageData(name,costString,areaString,normalizingConstant);
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
