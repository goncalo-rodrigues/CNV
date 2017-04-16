import math
from decimal import *


# TODO: Change the iteration predicate to one related with side!!! (Avoids problems with fractional conversion)
class PixelCalculator:
    def __init__(self, m, lst_pixel, lst_used, prop, side):
        self.m = m
        self.lst_pixel = lst_pixel
        self.lst_used = lst_used
        self.prop = prop
        self.side = side

    def next_pixel(self):
        next_col = math.floor(self.lst_pixel[1] + self.lst_used[1] + self.prop)

        lst_needed_pixels = []

        # Needed to eliminate ceiling bug
        needed_line = int(math.ceil(Decimal("{:.11f}".format(self.prop + self.lst_used[0]))))
        needed_col = int(math.ceil(Decimal("{:.11f}".format(self.prop + self.lst_used[1]))))

        # Gets the pixels needed for the calculation
        for i in range(needed_line):
            lst_needed_pixels.append([])
            for j in range(needed_col):
                lst_needed_pixels[i].append((self.lst_pixel[0] + i, self.lst_pixel[1] + j))

        # Calculates the value for the lb pixel table
        res = 0
        for i in range(needed_line):
            if i == needed_line - 1:
                tmp = math.modf(self.lst_pixel[0] + self.lst_used[0] + self.prop)[0]
                use_line = tmp if tmp > 0 else 1
            elif i == 0:
                use_line = 1 - self.lst_used[0]
            else:
                use_line = 1

            for j in range(needed_col):
                if j == needed_col - 1:
                    tmp = math.modf(self.lst_pixel[1] + self.lst_used[1] + self.prop)[0]
                    use_col = tmp if tmp > 0 else 1
                elif j == 0:
                    use_col = 1 - self.lst_used[1]
                else:
                    use_col = 1

                line_m = int(lst_needed_pixels[i][j][0])
                col_m = int(lst_needed_pixels[i][j][1])

                # FIXME: This formula is not good (but only for lb > than received matrix)
                res += use_line * use_col * self.m[line_m][col_m]

        # Updates the next pixel to be calculated
        if next_col < len(self.m):
            self.lst_pixel[1] = next_col
            self.lst_used[1] = math.modf(self.lst_pixel[1] + self.lst_used[1] + self.prop)[0]

        else:
            next_line = math.floor(self.lst_pixel[0] + self.lst_used[0] + self.prop)

            self.lst_pixel[0] = next_line
            self.lst_pixel[1] = 0
            self.lst_used[0] = math.modf(self.lst_pixel[0] + self.lst_used[0] + self.prop)[0]
            self.lst_used[1] = 0

        # print_matrix(lst_needed_pixels)
        # print "Calculated result: " + str(res) + "\n"

        return res


# TODO: Even and odd lines/columns (and vice-versa) have a bug!!!
def convert_to_square(m):
    n_lines = len(m)
    n_cols = len(m[0])
    no_padding = (n_cols + n_lines) % 2 == 0
    lines_pad = None
    cols_pad = None

    if n_cols > n_lines:
        sqr_side = n_cols if no_padding else n_cols + 1
        lines_pad = sqr_side/2 - n_lines/2
    else:
        sqr_side = n_lines if no_padding else n_lines + 1
        cols_pad = sqr_side / 2 - n_cols / 2

    new_m = [[0 for x in range(sqr_side)] for y in range(sqr_side)]

    if lines_pad is not None:
        cols_offset = 0 if no_padding else 1
        for i in range(n_lines):
            for j in range(n_cols):
                new_m[i + lines_pad][j + cols_offset] = m[i][j]

    else:
        lines_offset = 0 if no_padding else 1
        for i in range(n_lines):
            for j in range(n_cols):
                new_m[i + lines_offset][j + cols_pad] = m[i][j]

    return new_m


# TODO: Same problem as the function before
def revert_translation(m, sl, sc, sloff, scoff):
    n_lines = len(m)
    n_cols = len(m[0])

    lines_pad = (sl - n_lines) / 2
    cols_pad = (sc - n_cols) / 2
    add_lines = lines_pad + sloff
    add_cols = cols_pad + scoff

    new_m = [[0 for x in range(sc)] for y in range(sl)]

    # Copies the content to the new one
    for i in range(n_lines):
        for j in range(n_cols):
            new_m[i + add_lines][j + add_cols] = m[i][j]

    return new_m


def scale_to_store(side, m):
    init_size = len(m)

    # Represents the load balancer matrix
    ld = [[-1 for x in range(side)] for y in range(side)]

    pc = PixelCalculator(m, [0, 0], [0, 0], float(init_size) / float(side), side)

    for i in range(side):
        for j in range(side):
            ld[i][j] = pc.next_pixel()

    return ld


def print_matrix(m):
    n_lines = len(m)
    n_cols = len(m[0])

    for i in range(n_lines):
        line = ""
        for j in range(n_cols):
            line += str(m[i][j]) + " | "
        print line


"""
To try out
"""

n_lines = 6
n_cols = 6
test_m = [[5 for x in range(n_cols)] for y in range(n_lines)]

print "Test Matrix:"
print_matrix(test_m)

# print "\nConvert to square result:"
# print_matrix(convert_to_square(test_m))
#
# print "\nRevert translation result:"
# revert_res = revert_translation(test_m, 5, 3, 0, 0)
# print_matrix(revert_res)
#
# print "\nCombination of the previous two:"
# print_matrix(convert_to_square(revert_res))

print "\nScale to store result:"
print_matrix(scale_to_store(4, test_m))
