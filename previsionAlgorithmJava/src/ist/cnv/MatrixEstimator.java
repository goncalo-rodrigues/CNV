package ist.cnv;

import com.amazonaws.services.dynamodbv2.xspec.L;

import java.util.ArrayList;
import java.util.List;

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

    private Fraction[] used;
    private Fraction prop;

    // TODO: Check if it should get an estimation and not a matrix
    public MatrixEstimator(int[][] matrix, int sl, int sc, int wl, int wc, int wloff, int wcoff) {
        this.matrix = matrix;
        pixel = new int[] {0, 0};
        sideCounter = 1;
        used = new Fraction[] {new Fraction(0, 1), new Fraction(0, 1)};
        prop = new Fraction(matrix[0].length, SIDE);
        this.sl = sl;
        this.sc = sc;
        this.wl = wl;
        this.wc = wc;
        this.wloff = wloff;
        this.wcoff = wcoff;
    }

    private double nextPixel() {
        int nextCol = (int) floor(prop.add(pixel[COLUMN]).add(used[COLUMN]).toDouble());
        int neededLine = (int) ceil(prop.add(used[LINE]).toDouble());
        int neededCol = (int) ceil(prop.add(used[COLUMN]).toDouble());

        // Calculates the value for the load balancer table
        double res = 0;
        for(int i = 0; i < neededLine; i++) {
            Fraction useLine = new Fraction(1,1);

            if(i == neededLine - 1) {
                Fraction tmpLine = used[LINE].add(prop);

                if(tmpLine.toDouble() < 1 || (tmpLine.removeInteger().toDouble() == 0 && SIDE >= matrix[0].length))
                    useLine = prop;

                else if(tmpLine.toDouble() != 0)
                    useLine = tmpLine;
            }

            // FIXME: Is this right?
            else if(i == 0)
                useLine = used[LINE].inverseSub(1);

            int l = pixel[LINE] + i;
            for(int j = 0; j < neededCol; j++) {
                Fraction useCol = new Fraction(1,1);

                if(j == neededCol - 1) {
                    Fraction tmpCol = used[COLUMN].add(prop);

                    if(tmpCol.toDouble() < 1 || (tmpCol.removeInteger().toDouble() == 0 && SIDE >= matrix[0].length))
                        useCol = prop;

                    else if(tmpCol.toDouble() != 0)
                        useCol = tmpCol;
                }

                // FIXME: Is this right?
                else if(j == 0)
                    useCol = used[COLUMN].inverseSub(1);

                res += useLine.mul(useCol).mul(matrix[l][pixel[COLUMN] + j]).toDouble();
            }
        }

        // Updates the next pixel to be calculated
        if(nextCol < matrix[0].length && sideCounter < SIDE) {
            pixel[COLUMN] = nextCol;
            used[COLUMN] = used[COLUMN].add(pixel[COLUMN]).add(prop);
            used[COLUMN].removeInteger();
            sideCounter++;
        }

        else {
            int nextLine = (int) floor(used[LINE].add(pixel[LINE]).add(prop).toDouble());
            pixel = new int[] {nextLine, 0};

            used[LINE] = used[LINE].add(pixel[LINE]).add(prop);
            used[LINE].removeInteger();

            used[COLUMN] = new Fraction(0,1);
            sideCounter = 1;
        }

        return res;
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
            Fraction ratio = new Fraction(nCols, sqrSide);
            for(int i = 0; i < nLines; i++) {
                int current = 0;
                Fraction used = new Fraction(0,1);
                Fraction value;

                for(int j = 0; j < sqrSide; j++) {
                    if(ratio.toDouble() > used.inverseSub(1).toDouble()) {
                        Fraction n1 = used.inverseSub(1).mul(matrix[i][current]);
                        Fraction n2 = used.add(ratio).sub(1).mul(matrix[i][current + 1]);
                        value = n1.add(n2);
                        current += 1;
                        used = used.add(ratio).sub(1);
                    }

                    else {
                        value = ratio.mul(matrix[i][current]);
                        used = used.add(ratio);
                    }

                    // TODO: Check if we want matrices of integers or doubles!!!
                    tmpMatrix[(int) (i + linesPad)][j] = (int) value.toDouble();
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

    // TODO: Check if this can be done in the nextPixel!!!
    // TODO: Add the methods from the main, making all the necessary transformations!!!
    public void scaleToStore() {
        if(matrix.length == SIDE)
            return;

        int[][] res = new int[SIDE][SIDE];

        for(int i = 0; i < SIDE; i++)
            for(int j = 0; j < SIDE; j++)
                // TODO: Check if we want matrices of integers or doubles!!!
                res[i][j] = (int) nextPixel();

        matrix = res;
    }

    // FIXME: Just for testing purposes!!!
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
        int LINES = 31;
        int COLS = 32;

        int[][] matrix = new int[LINES][COLS];

        // Changes the matrix
        for(int i = 0; i < LINES; i++)
            for(int j = 0; j < COLS; j++)
                matrix[i][j] = i + j;

        MatrixEstimator me = new MatrixEstimator(matrix, LINES, COLS, LINES - 1, COLS - 1, 1, 1);

        me.revertTranslation();
//        me.printMatrix();
//
//        System.out.println("\n==================================================================================\n");

        me.convertToSquare();
//        me.printMatrix();
//
//        System.out.println("\n==================================================================================\n");

        me.scaleToStore();
        me.printMatrix();
    }
}
