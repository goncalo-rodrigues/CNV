package ist.cnv;

import java.util.List;
import java.util.ArrayList;

/**
 * Created on 15-05-2017.
 */

public class FastMatrixEstimator {
    private static final int SIDE = 40;
    private static final int LINE = 0;
    private static final int COLUMN = 1;

    private int[] topLeft;
    private int[] bottomRight;
    private boolean wasCalculated = false;

    // Request info
    private int sl;
    private int sc;
    private int wl;
    private int wc;
    private int wloff;
    private int wcoff;

    private double metric;

    public FastMatrixEstimator(long metric, int sl, int sc, int wl, int wc, int wloff, int wcoff) {
        this.sl = sl;
        this.sc = sc;
        this.wl = wl;
        this.wc = wc;
        this.wloff = wloff;
        this.wcoff = wcoff;
        this.metric = metric;

        // Metrics to be converted
        topLeft = new int[] {sl - wl - wloff, wcoff};
        bottomRight = new int[] {sl - wloff, wcoff + wc};
    }

    private void convertToSquare() {
        boolean noPadding = (sc + sl) % 2 == 0;
        int sqrSide;
        int pad;

        // It is already a square
        if(sc == sl)
            return;

        if(sc > sl) {
            sqrSide = noPadding ? sc : (sc + 1);
            pad = (int) ( ((double) sqrSide)/2 - ((double) sl)/2);
            topLeft[LINE] = topLeft[LINE] + pad;
            bottomRight[LINE] = bottomRight[LINE] + pad;

            if(!noPadding) {
                topLeft[COLUMN] --;
                bottomRight[COLUMN] ++;
            }
        }

        else {
            sqrSide = noPadding ? sl : (sl + 1);
            pad = (int) ( ((double) sqrSide)/2 - ((double) sc)/2);
            topLeft[COLUMN] = topLeft[COLUMN] + pad;
            bottomRight[COLUMN] = bottomRight[COLUMN] + pad;

            if(!noPadding) {
                topLeft[LINE] --;
                bottomRight[LINE] ++;
            }
        }

        if(noPadding)
            metric = metric / (double) (wl * wc);
        else
            metric = metric / ( (bottomRight[COLUMN] - topLeft[COLUMN]) * (bottomRight[LINE] - topLeft[LINE]) );

        sc = sl = sqrSide;
    }

    private void toLBMatrix() {
        double ratio = (double) SIDE / (double) sc;

        topLeft[LINE] = (int) Math.ceil(topLeft[LINE] * ratio);
        topLeft[COLUMN] = (int) Math.ceil(topLeft[COLUMN] * ratio);

        bottomRight[LINE] = (int) Math.ceil(bottomRight[LINE] * ratio);
        bottomRight[COLUMN] = (int) Math.ceil(bottomRight[COLUMN] * ratio);

        metric *= ratio;
    }

    public List<int[]> getTransformedRequest() {
        if(!wasCalculated) {
            convertToSquare();
            toLBMatrix();
            wasCalculated = true;
        }

        ArrayList<int[]> res = new ArrayList<>();
        res.add(topLeft);
        res.add(bottomRight);

        return res;
    }

    // TODO: Just for debugging purposes!!!
    public static void main(String[] args) {
        int LINES = 15000;
        int COLS = LINES;

        FastMatrixEstimator fme = new FastMatrixEstimator(LINES * COLS, LINES, COLS, LINES / 2, COLS / 2, 0,
                0);

        for(int[] el : fme.getTransformedRequest())
            System.out.println("(" + el[0] + ", " + el[1] + ")");

    }
}
