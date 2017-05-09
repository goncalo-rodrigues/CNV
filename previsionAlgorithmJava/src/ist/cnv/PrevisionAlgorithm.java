package ist.cnv;


import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * Created by ant on 08-05-2017.
 */
public class PrevisionAlgorithm {
    ArrayList<File> files = new ArrayList<>();

    public PrevisionAlgorithm(ArrayList<String> filesNames){
        for(String fileName: filesNames)
            files.add(new File(fileName));

    }

    //http://<load-balancer-DNS-name>/r.html?
    // f=<model-filename>
    // &sc=<scene-columns>&sr=<scene-rows>          //number of collumns and rows in table
    // &wc=<window-columns>&wr=<window-rows>        //resoluçao final
    // &coff=<column-offset>&roff=<row-offset>      //|_


    public int estimateCost(String fileName, int sc, int sr,
                            int wc, int wr, int coof,int roof){
        int requestPixels = wr*wc;
        float propotion = requestPixels / 10000;

        //TODO @Nuno the conversion is here
        for(File file:files)
            if(file.name.equals(fileName))
                return (int) (propotion *(float) estimateCost(0,0,39,39,file));//TODO fix
        return 0;
    }


    private int estimateCost(int x1,int y1, int x2, int y2,File file){
        int prevision = 0;
        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                prevision += file.cost[y][x];
            }
        }
        return prevision;
    }


    public void insertData(String fileName, int sc, int sr,
                            int wc, int wr, int coof,int roof,int cost){
        int requestPixels = wr*wc;
        float propotion = requestPixels / 10000;

        //TODO @Nuno the conversion is here
        for(File file : files)
            if(file.name.equals(fileName))
                insertData(0 ,0 ,9,9,(int)(cost/propotion),file);//TODO fix
    }


    private void insertData(int x1,int y1, int x2, int y2, int requestCost,File file){
        int totalArea = (abs(x1-x2))*(abs(y1-y2));
        int knownCost = 0;
        int nElemetsToChange = 0;
        int costToSplit = 0;
        int eachCost = 0;

        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                if(file.area[y][x] < totalArea)
                    knownCost += file.cost[y][x];
                else
                    nElemetsToChange += 1;
            }
        }

        if(nElemetsToChange > 0) {
            costToSplit = requestCost - knownCost;
            if (costToSplit < 0)
                costToSplit = nElemetsToChange;
            eachCost = costToSplit/nElemetsToChange;
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
                "\neachCost:"+eachCost);
    }


}
