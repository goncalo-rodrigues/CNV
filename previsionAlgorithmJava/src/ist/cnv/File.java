package ist.cnv;

/**
 * Created on 09-05-2017.
 */

public class File {
    private static int TSIZE = 40;          // Table size in DB ex: 40x40
    private static int SAVESUNTILSEND = 15; // The number of updates in the file until send to dynamo
    private static int STARTINGCOST = 11;   // The predefined cost each square have until we have some information
    String name;                            // The name of the image file
    public int[][] cost;                    // The cost of each part of the image
    public int[][] area;                    // The are wich have the request that make the change in the cost table
    public double normalizingConstant;      // Relative contant to file 1 in terms of time/Instructions
    DynamoDBConnection dynamoConnection;

    private int acumulatedSaves = 0;


    public File(String name,double normalizingConstant ,DynamoDBConnection dynamoConnection){
        this.name = name;
        this.normalizingConstant = normalizingConstant;
        this.dynamoConnection = dynamoConnection;
        load();
    }


    public void load(){
        //check if the image already exists in the database
        String result[] = dynamoConnection.getImageData(this.name);
        if (result != null) {
            cost = stringToIntList(result[0]);
            area = stringToIntList(result[1]);
            normalizingConstant = Double.parseDouble(result[2]);
        }
        //if dont exists it create a new one
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


    //Saves are made periodically
    public void save(){
        acumulatedSaves ++;
        if (acumulatedSaves >= SAVESUNTILSEND) {
            forceSave();
            acumulatedSaves = 0;
        }
    }


    //Saves are made periodically, to make it now use this
    public void forceSave(){
        String costString = intListToString(cost);
        String areaString = intListToString(area);
        dynamoConnection.saveImageData(name,costString,areaString,normalizingConstant);
    }


    //Transform an arraylist in a String to save it in dynamo
    private String intListToString(int[][] intArray){
        String output = "";
        for(int x = 0; x < TSIZE; x++)
            for(int y = 0; y < TSIZE ; y++)
                output += intArray[y][x]+";";
        return output;
    }


    //Transform an arraylist in String format to real arraylist
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
