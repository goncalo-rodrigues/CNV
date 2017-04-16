import plotly
from plotly.graph_objs import  Layout
import random
import math
import numpy as np
import plotly.graph_objs as go
from algorithm.imageLoader import print_image
from imageLoader import request_raytracer
import pandas

RED = '\033[31m'
GREEN = '\033[32m'
OKGREEN = '\033[92m'
OKBLUE = '\033[94m'
ENDC = '\033[0m'


#------this is not mine---

def _should_round_down(val: float):
    if val < 0:
        return ((val * -1) % 1) < 0.5
    return (val % 1) < 0.5


def _round(val: float, ndigits = 0):
    if ndigits > 0:
        val *= 10 ** (ndigits - 1)

    is_positive = val > 0
    tmp_val = val
    if not is_positive:
        tmp_val *= -1

    rounded_value = math.floor(tmp_val) if _should_round_down(val) else    math.ceil(tmp_val)
    if not is_positive:
        rounded_value *= -1

    if ndigits > 0:
        rounded_value /= 10 ** (ndigits - 1)

    return rounded_value

#--------------------------



# return the Preciso of the gess
def resultPrecision(v1, v2):
    # abs(esperado - real) / real
    if v1 == v2:
        return 100
    elif v1 ==0 or v2 == 0:
        return 0
    elif (v1 > v2):
        return v2 / v1 * 100
    else:
        return v1 / v2 * 100


# Emulates a completely random request
def randomImput(sizex, sizey):
    r = random.SystemRandom()
    xs = r.randint(1, sizex)
    ys = r.randint(1, sizey)
    x1 = r.randint(0, sizex - xs)
    x2 = x1 + xs - 1
    y1 = r.randint(0, sizey - ys)
    y2 = y1 + ys - 1
    result = (x1, y1, x2, y2)
    return result


# Create a completly random table
def createRandomTable(Xsize, Ysize, maxCost):
    r = random.SystemRandom()
    table = [[r.randint(1, maxCost) for x in range(Xsize)] for y in range(Ysize)]
    return table


# This method simulates the execution of the raytrace returning the cost
def SimulateRayTracer(x1, y1, x2, y2, realCostTable):
    cost = 0
    for x in range(x1, x2 + 1):
        for y in range(y1, y2 + 1):
            cost += realCostTable[y][x]
    return cost

def RunRayTracer(x1, y1, x2, y2, realCostTable, fname):
    y2 = y2 + 1
    x2 = x2 + 1
    width = x2-x1
    height = y2-y1
    id = request_raytracer(fname, len(realCostTable),len(realCostTable),height, width, height - y2, x1)
    cols = pandas.read_csv('~/testcnv/raytracer-master/dynamic_%d.txt' % id)
    cost = cols.loc[cols['description'] == 'dot', 'value'].values[0]
    return cost

# print the 2 tables side by side
def printTablesBySide(t1, expected):
    nlines = len(t1)
    ncolumn = len(t1[0])
    for x in range(0, nlines):
        line = ""
        lLine = ""
        rLine = ""
        for y in range(0, ncolumn * 2):
            lLine += "-"
            rLine += "-"
        line += lLine + "  " + rLine
        print(line)

        line = ""
        lLine = ""
        rLine = ""
        for y in range(0, ncolumn):
            pred = t1[x][y].cost
            expe = expected[x][y]
            if abs(pred - expe) > 2:
                lLine += "|" + RED + str(_round(pred)) + ENDC
            elif abs(pred - expe) == 0:
                lLine += "|" + GREEN + str(_round(pred)) + ENDC
            else:
                lLine += "|" + OKBLUE + str(_round(pred)) + ENDC
            rLine += "|" + str(expe)

        line += lLine + "  " + rLine
        print(line)
    print("\n")


