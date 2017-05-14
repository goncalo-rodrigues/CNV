package ist.cnv;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> imagesNames = new ArrayList<>();
        imagesNames.add("img1");
        imagesNames.add("img2");
	    PrevisionAlgorithm oracle = new PrevisionAlgorithm(imagesNames);
	    //Light request
        System.out.println(oracle.estimateCost("img1",400,300,400,
                300, 0,0));
        oracle.insertData("img1",400,300,400,
                300, 0,0,1000);

        //Medium request
        System.out.println(oracle.estimateCost("img1",8000,6000,1600,
                1200, 0,6400));
                oracle.insertData("img1",8000,6000,1600,
                1200, 0,6400 ,100000);

        //Heavy request
        System.out.println(oracle.estimateCost("img1",20000,15000,3250,
                2500, 5000,6500));
        oracle.insertData("img1",20000,15000,3250,
                2500, 5000,6500 ,100000);


        oracle.insertData("img4",50,50,100,
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

