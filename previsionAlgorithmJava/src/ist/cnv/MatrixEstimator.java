package ist.cnv;

import static java.lang.Math.*;

/**
 * Created on 11-05-2017.
 */

public class MatrixEstimator {
    private static final int SIDE = 40;
    private static final int LINE = 0;
    private static final int COLUMN = 1;

    private int[][] matrix;
    private int[] pixel;
    private int sideCounter;
    private int sl;
    private int sc;
    private int wl;
    private int wc;
    private int wloff;
    private int wcoff;

    private double[] used;
    private double prop;

    public MatrixEstimator(long metric, int sl, int sc, int wl, int wc, int wloff, int wcoff) {
        pixel = new int[] {0, 0};
        sideCounter = 1;
        used = new double[] {0, 0};
        this.sl = sl;
        this.sc = sc;
        this.wl = wl;
        this.wc = wc;
        this.wloff = wloff;
        this.wcoff = wcoff;

        // Converts the metric to a matrix
        matrix = new int[wl][wc];
        double ratio = (double) metric / (double) (wl * wc);
        for(int i = 0; i < wl; i++)
            for(int j = 0; j < wc; j++)
                matrix[i][j] = (int) ratio;
    }

    private int nextPixel() {
        int nextCol = (int) floor(prop + pixel[COLUMN] + used[COLUMN]);
        int neededLine = (int) ceil(prop + used[LINE]);
        int neededCol = (int) ceil(prop + used[COLUMN]);

        // Prevents index out of bounds
        while(pixel[LINE] + neededLine > matrix.length)
            neededLine --;
        while(pixel[COLUMN] + neededCol > matrix.length)
            neededCol --;

        // Calculates the value for the load balancer table
        double res = 0;
        for(int i = 0; i < neededLine; i++) {
            double useLine = 1;

            if(i == neededLine - 1) {
                double tmpLine = used[LINE] + prop;

                if(tmpLine < 1 || ( (tmpLine -= (long) tmpLine) == 0 && SIDE >= matrix[0].length))
                    useLine = prop;

                else if(tmpLine != 0)
                    useLine = tmpLine;
            }

            // When it needs more than one pixel
            else if(i == 0)
                useLine = 1 - used[LINE];

            int l = pixel[LINE] + i;
            for(int j = 0; j < neededCol; j++) {
                double useCol = 1;

                if(j == neededCol - 1) {
                    double tmpCol = used[COLUMN] + prop;

                    if(tmpCol < 1 || ( (tmpCol -= (long) tmpCol) == 0 && SIDE >= matrix[0].length))
                        useCol = prop;

                    else if(tmpCol != 0)
                        useCol = tmpCol;
                }

                // When it needs more than one pixel
                else if(j == 0)
                    useCol = 1 - used[COLUMN];

                res += useLine * useCol * matrix[l][pixel[COLUMN] + j];
            }
        }

        // Updates the next pixel to be calculated
        if(sideCounter < SIDE) {
            pixel[COLUMN] = nextCol;
            used[COLUMN] = used[COLUMN] + pixel[COLUMN] + prop;
            used[COLUMN] -= (long) used[COLUMN];
            sideCounter++;
        }

        else {
            int nextLine = (int) floor(used[LINE] + pixel[LINE] + prop);
            pixel = new int[] {nextLine, 0};

            used[LINE] = used[LINE] + pixel[LINE]  + prop;
            used[LINE] -= (long) used[LINE];

            used[COLUMN] = 0;
            sideCounter = 1;
        }

        return (int) ceil(res / (neededLine * neededCol));
    }

    private void convertToSquare() {
        int nLines = matrix.length;
        int nCols = matrix[0].length;
        boolean rotated = false;
        boolean noPadding = (nLines + nCols) % 2 == 0;

        // Non-initialized vars
        double linesPad;
        int sqrSide;

        if(nCols == nLines)
            return;

        if(nLines > nCols) {
            matrix = rotate(matrix);
            rotated = true;
            int n = nLines;
            nLines = nCols;
            nCols = n;
        }

        sqrSide = noPadding ? nCols : (nCols + 1);
        linesPad = (double) sqrSide/2 - (double) nLines/2;

        int[][] tmpMatrix = new int[sqrSide][sqrSide];

        if(noPadding) {
            for(int i = 0; i < nLines; i++)
                for(int j = 0; j < nCols; j++)
                    tmpMatrix[(int) (i + linesPad)][j] = matrix[i][j];
        }

        else {
            double ratio = (double) nCols / (double) sqrSide;
            for(int i = 0; i < nLines; i++) {
                int current = 0;
                double used = 0;
                double value;

                for(int j = 0; j < sqrSide; j++) {
                    if(ratio > 1 - used) {
                        value = (1 - used) * matrix[i][current] + (used + ratio - 1) * matrix[i][current + 1];
                        current += 1;
                        used = used + ratio - 1;
                    }

                    else {
                        value = ratio * matrix[i][current];
                        used = used + ratio;
                    }

                    tmpMatrix[(int) (i + linesPad)][j] = (int) round(value);
                }
            }
        }

        if(rotated)
            tmpMatrix = revertRotation(tmpMatrix);

        matrix = tmpMatrix;
    }

    private void revertTranslation() {
        if(sc == wc && sl == wl)
            return;

        int maxLineIndex = sl - 1;
        int maxLineIndexFirst = wl - 1;

        // Creates a new matrix to store the conversion result
        int[][] tmpMatrix = new int[sl][sc];

        for(int i = 0; i < wl; i++) {
            // To reduce the number of math operations with the same result
            int fromCol = maxLineIndexFirst - i;
            int toCol = maxLineIndex - i - wloff;
            for (int j = 0; j < wc; j++)
                tmpMatrix[toCol][j + wcoff] = matrix[fromCol][j];
        }

        matrix = tmpMatrix;
    }

    /*
     * Aux functions
     */

    // Makes a rotation
    private static int[][] rotate(int[][] matrix) {
        int[][] tmpMatrix;
        int nLines = matrix.length;
        int nCols = matrix[0].length;

        tmpMatrix = new int[nCols][nLines];

        // Does the rotation
        for(int i = 0; i < nCols; i++)
            for(int j = 0; j < nLines; j++)
                tmpMatrix[i][j] = matrix[j][i];

        return tmpMatrix;
    }

    // Reverts the rotation (but now it is a square)
    private static int[][] revertRotation(int[][] matrix) {
        int side = matrix.length;
        for(int i = 0; i < side; i++) {
            for(int j = i; j < side; j++) {
                int n = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = n;
            }
        }

        return matrix;
    }

    public void scaleToStore() {
        if(matrix.length == SIDE)
            return;

        revertTranslation();
        convertToSquare();

        int[][] res = new int[SIDE][SIDE];
        prop = (double) matrix[0].length / (double) SIDE;
        for(int i = 0; i < SIDE; i++)
            for(int j = 0; j < SIDE; j++)
                res[i][j] = nextPixel();

        matrix = res;
    }

    public void printMatrix() {
        long nLines = matrix.length;
        long nCols = matrix[0].length;

        for(int i = 0; i < nLines; i++) {
            StringBuilder line = new StringBuilder("|");
            for(int j = 0; j < nCols; j++)
                line.append(String.valueOf(matrix[i][j]) + "|");
            System.out.println(line);
        }
    }

    // FIXME: Just for testing purposes!!!
    public static void main(String[] args) {
        int LINES = 15000;
        int COLS = LINES;

        MatrixEstimator me = new MatrixEstimator(LINES * COLS, LINES, COLS, LINES, COLS, 0, 0);
        
        me.scaleToStore();
        me.printMatrix();
    }
}
