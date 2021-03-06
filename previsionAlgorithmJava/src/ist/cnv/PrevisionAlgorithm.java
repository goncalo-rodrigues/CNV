package ist.cnv;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;


public class PrevisionAlgorithm {
    ArrayList<File> files = new ArrayList<>();
    ArrayList<String> fileNames = new ArrayList<>();
    DynamoDBConnection dynamo;


    public PrevisionAlgorithm(ArrayList<String> filesNames){
        dynamo = new DynamoDBConnection();
        this.fileNames = filesNames;
        for(String fileName: filesNames)
            files.add(new File(fileName,1,dynamo));
    }


    public void addFile(String fileName, double normalizingConstant){
        files.add(new File(fileName,normalizingConstant, dynamo));
        fileNames.add(fileName);
    }


    //Estimate the cost of one request
    public long estimateCost(String fileName, int sc, int sr, int wc, int wr, int coff,int roff){
        long requestPixels = sr*sc;
        double propotion = (double) requestPixels / (double) 1600;
        if(!fileNames.contains(fileName)) {
            files.add(new File(fileName,1, dynamo));
            fileNames.add(fileName);
        }
        List<int[]> coords = new FastMatrixEstimator(1000 , sr,  sc, wr, wc, roff, coff).getTransformedRequest();

        for(File file:files)
            if(file.name.equals(fileName))
                return (long) (propotion * estimateCost(coords.get(0)[0] , coords.get(0)[1] ,
                        coords.get(1)[0] , coords.get(1)[1],file) );
        return 0;
    }


    //Estimate the cost of one request
    private int estimateCost(int x1,int y1, int x2, int y2,File file){
        int prevision = 0;
        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                prevision += file.cost[y][x];
            }
        }
        return prevision;
    }


    //Insert Received metrics data on cash and after 5 times it sabes on dynamoDB
    public void insertData(String fileName, int sc, int sr,
                            int wc, int wr, int coff,int roff,long cost){
        int requestPixels = sr*sc;
        float propotion = requestPixels / 1600;
        boolean changed;

        List<int[]> coords = new FastMatrixEstimator(cost , sr,  sc, wr, wc, roff, coff).getTransformedRequest();

        if(!fileNames.contains(fileName)){
            files.add(new File(fileName,1,dynamo));
            fileNames.add(fileName);
        }

        for(File file : files)
            if(file.name.equals(fileName)) {
                changed = insertData(coords.get(0)[0] , coords.get(0)[1] , coords.get(1)[0] ,
                        coords.get(1)[1] ,  (int) (cost / propotion), file);
                if(changed)
                    file.save();

            }
    }


    //Information cached of all images is stored on the database
    public void saveAll(){
        for(File file : files)
            file.forceSave();
    }


    //Insert Received metrics data on cash and after 5 times it sabes on dynamoDB
    private boolean insertData(int x1,int y1, int x2, int y2, int requestCost,File file){
        requestCost =(int)( (float)requestCost * file.normalizingConstant );
        int totalArea = (abs(x1-x2))*(abs(y1-y2));
        int knownCost = 0;
        int nElemetsToChange = 0;
        int costToSplit = 0;
        int eachCost = 0;

        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                if(file.area[y][x] <= totalArea)
                    knownCost += file.cost[y][x];
                else
                    nElemetsToChange += 1;
            }
        }

        if(nElemetsToChange > 0) {
            costToSplit = requestCost - knownCost;
            if (costToSplit < 0)
                costToSplit = nElemetsToChange;
            eachCost = (int) Math.round((double)costToSplit/nElemetsToChange);
            for(int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    if(file.area[y][x] > totalArea){
                        file.area[y][x] = totalArea;
                        file.cost[y][x] = eachCost;
                    }
                }
            }
        }
        System.out.println("totalArea:"+totalArea+"\nnElemetsToChange:"+nElemetsToChange+"\ncostToSplit: "+costToSplit+
                "\neachCost:"+eachCost+"\n");
        return nElemetsToChange>0;
    }


}
