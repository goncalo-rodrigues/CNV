package ist.cnv;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> imagesNames = new ArrayList<>();
        imagesNames.add("img1");
        imagesNames.add("img2");
	    PrevisionAlgorithm oracle = new PrevisionAlgorithm(imagesNames);
        System.out.println(oracle.estimateCost("img1", 100, 100,
                100,100, 0,0));

        oracle.insertData("img1",50,50,100,
        100, 0,0,100000);
        oracle.insertData("img1",50,50,100,
                100, 0,0,100000);
        oracle.insertData("img2",8,8,100,
                100, 10,10,9000);
        System.out.println(oracle.estimateCost("img1", 10, 10,
                100,100, 0,0));

        System.out.println(oracle.estimateCost("img1", 10, 10,
                1000,1000, 0,0));

        oracle.saveAll();
    }
}

//http://<load-balancer-DNS-name>/r.html?
// f=<model-filename>
// &sc=<scene-columns>&sr=<scene-rows>          //number of collumns and rows in table
// &wc=<window-columns>&wr=<window-rows>        //resoluçao final
// &coff=<column-offset>&roff=<row-offset>      //|_

