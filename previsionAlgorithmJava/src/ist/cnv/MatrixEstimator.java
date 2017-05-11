package ist.cnv;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

/**
 * Created on 11-05-2017.
 */

public class MatrixEstimator {
    private static final int SIDE = 40;
    private static final int LINE = 0;
    private static final int COLUMN = 1;

    private int[][] matrix;
    private long[] pixel;
    private long sideCounter;

    private Fraction[] used;
    private Fraction prop;

    public MatrixEstimator(int[][] matrix) {
        this.matrix = matrix;
        pixel = new long[] {0, 0};
        sideCounter = 1;
        used = new Fraction[] {new Fraction(0, 1), new Fraction(0, 1)};
        prop = new Fraction(matrix[0].length, SIDE);
    }

    public double nextPixel() {
        long nextCol = (long) floor(prop.add(pixel[COLUMN]).add(used[COLUMN]).toDouble());
        List<List<long[]>> neededPixels = new ArrayList<>();
        long neededLine = (long) ceil(prop.add(used[LINE]).toDouble());
        long neededCol = (long) ceil(prop.add(used[COLUMN]).toDouble());

        // Gets the needed pixels for the calculation
        for(long i = 0; i < neededLine; i++) {
            List<long[]> tmpList = new ArrayList<>();
            neededPixels.add(tmpList);

            long l = pixel[LINE] + i;
            for(long j = 0; j < neededCol; j++)
                tmpList.add(new long[] {l, pixel[LINE] + j});
        }

        // Calculates the value for the load balancer table
        double res = 0;
        for(long i = 0; i < neededLine; i++) {
            Fraction useLine;

            if(i == neededLine - 1) {
                useLine = used[LINE].add(prop);

                if(useLine.toDouble() < 0)
                    useLine = prop;
                else
                    useLine.removeInteger();

                if(useLine.toDouble() == 0) {
                    if(SIDE < matrix[0].length)
                        useLine = new Fraction(1,1);
                    else
                        useLine = prop;
                }
            }

            else if(i == 0)
                useLine = used[LINE].inverseSub(1);
            else
                useLine = new Fraction(1,1);

            for(long j = 0; j < neededCol; j++) {
                Fraction useCol;
                if(j == neededCol - 1) {
                    useCol = used[COLUMN].add(prop);

                    if(useCol.toDouble() < 0)
                        useCol = prop;
                    else
                        useCol.removeInteger();

                    if(useCol.toDouble() == 0) {
                        if(SIDE < matrix[0].length)
                            useCol = new Fraction(1, 1);
                        else
                            useCol = prop;
                    }
                }

                else if(j == 0)
                    useCol = used[COLUMN].inverseSub(1);
                else
                    useCol = new Fraction(1, 1);

                long lineM = neededPixels.get((int) i).get((int) j)[LINE];
                long colM = neededPixels.get((int) i).get((int) j)[COLUMN];

                res += useLine.mul(useCol).mul(matrix[ (int) lineM][(int) colM]).toDouble();
            }
        }

        // TODO: Add the "Updates the next pixel to be calculated" part


        return res;
    }

    // FIXME: Just for testing purposes!!!
    public static void main(String[] args) {
        int[][] matrix = new int[80][80];
        MatrixEstimator me = new MatrixEstimator(matrix);
        me.nextPixel();
    }
}
