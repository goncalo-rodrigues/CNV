
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


def print_matrix(m):
    n_lines = len(m)
    n_cols = len(m[0])

    for i in range(n_lines):
        line = ""
        for j in range(n_cols):
            line += str(m[i][j]) + " | "
        print line


n_lines = 2
n_cols = 4
test_m = [[5 for x in range(n_cols)] for y in range(n_lines)]

print "Test Matrix:"
print_matrix(test_m)

print "\nConvert to Square result:"
print_matrix(convert_to_square(test_m))

print "\nRevert translation result:"
revert_res = revert_translation(test_m, 2, 6, 0, 0)
print_matrix(revert_res)

print "\nCombination of the previous two:"
print_matrix(convert_to_square(revert_res))
