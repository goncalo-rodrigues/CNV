package ist.cnv;

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

        // This should be treated as an error value
        return -1;
    }

    // FIXME: Just for testing purposes!!!
    public static void main(String[] args) {
        int[][] matrix = new int[80][80];
        MatrixEstimator me = new MatrixEstimator(matrix);
        me.nextPixel();
    }

//    Algorithm in python
//    next_col = int(math.floor(self.lst_pixel[1] + self.lst_used[1] + self.prop))
//
//    lst_needed_pixels = []
//    needed_line = int(math.ceil(self.prop + self.lst_used[0]))
//    needed_col = int(math.ceil(self.prop + self.lst_used[1]))
//
//            # Guarantees that the indexes are inside the matrix
//        while self.lst_pixel[0] + needed_line > len(self.m):
//    needed_line -= 1
//            while self.lst_pixel[1] + needed_col > len(self.m):
//    needed_col -= 1
//
//            # Gets the pixels needed for the calculation
//        for i in range(needed_line):
//    l = self.lst_pixel[0] + i
//
//            lst_needed_pixels.append([])
//                    for j in range(needed_col):
//    c = self.lst_pixel[1] + j
//    lst_needed_pixels[i].append((l, c))
//
//            # Calculates the value for the lb pixel table
//    res = 0
//            for i in range(needed_line):
//            if i == needed_line - 1:
//    use_line = math.modf(self.lst_used[0] + self.prop)
//
//            if use_line[1] == 0:
//    use_line = self.prop
//                else:
//    use_line = use_line[0]
//
//            if use_line == 0:
//            if self.side < len(self.m):
//    use_line = 1
//            else:
//    use_line = self.prop
//    elif i == 0:
//    use_line = 1 - self.lst_used[0]
//            else:
//    use_line = 1
//
//            for j in range(needed_col):
//            if j == needed_col - 1:
//    use_col = math.modf(self.lst_used[1] + self.prop)
//
//            if use_col[1] == 0:
//    use_col = self.prop
//                    else:
//    use_col = use_col[0]
//
//            if use_col == 0:
//            if self.side < len(self.m):
//    use_col = 1
//            else:
//    use_col = self.prop
//    elif j == 0:
//    use_col = 1 - self.lst_used[1]
//            else:
//    use_col = 1
//
//    line_m = int(lst_needed_pixels[i][j][0])
//    col_m = int(lst_needed_pixels[i][j][1])
//
//    res += float(use_line * use_col * self.m[line_m][col_m])
//
//        # Updates the next pixel to be calculated
//        if next_col < len(self.m) and self.lb_side_counter < self.side:
//    self.lst_pixel[1] = next_col
//    self.lst_used[1] = Fraction(self.lst_pixel[1] + self.lst_used[1] + self.prop)
//
//    self.lst_used[1] = Fraction(self.lst_used[1]._numerator % self.lst_used[1]._denominator,
//                                self.lst_used[1]._denominator)
//
//    self.lb_side_counter += 1
//
//            else:
//    next_line = int(math.floor(self.lst_pixel[0] + self.lst_used[0] + self.prop))
//
//    self.lst_pixel[0] = next_line
//    self.lst_pixel[1] = 0
//    self.lst_used[0] = Fraction(self.lst_pixel[0] + self.lst_used[0] + self.prop)
//
//    self.lst_used[0] = Fraction(self.lst_used[0]._numerator % self.lst_used[0]._denominator,
//                                self.lst_used[0]._denominator)
//
//    self.lst_used[1] = 0
//    self.lb_side_counter = 1
//
//            return res
}
