import pprint
import random
import decimal
import math
from datetime import datetime

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

def _round(val: float, ndigits=0):
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

class TableElement:
    def __init__(self, cost, level, group):
        self.cost = cost
        self.level = level
        self.group = group


def createTable(Xsize, Ysize):
    totalpixel = Xsize*Ysize+1;
    table = [[TableElement(0,totalpixel,0) for x in range(Xsize)] for y in range(Ysize)]
    return table


def createRandomTable(Xsize, Ysize):
    dt = datetime.now()
    dt2 = datetime.now()
    random.seed(dt.microsecond-dt2.microsecond)
    table = [[random.randint(1, 9) for x in range(Xsize)] for y in range(Ysize)]
    return table


def printTablesBySide(t1, expected):
    nlines = len(t1)
    ncolumn = len(t1[0])
    for x in range(0, nlines):
        line = ""
        lLine = ""
        rLine = ""
        for y in range(0, ncolumn*2):
            lLine += "-"
            rLine += "-"
        line += lLine+"  "+rLine
        print(line)

        line = ""
        lLine = ""
        rLine = ""
        for y in range(0,ncolumn):
            pred = t1[x][y].cost
            expe = expected[x][y]
            if abs(pred - expe) > 2:
                lLine += "|"+RED+str(_round(pred)) + ENDC
            elif abs(pred - expe) == 0:
                lLine += "|" + GREEN + str(_round(pred)) + ENDC
            else:
                lLine += "|" + OKBLUE + str(_round(pred)) + ENDC
            rLine += "|"+str(expe)

        line += lLine + "  " + rLine
        print(line)
    print("\n")

def estimatecost(x1, y1, x2, y2, previsionTable):
    prevision = 0
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            prevision += previsionTable[y][x].cost
    return prevision

def SimulateRayTracer(x1, y1, x2, y2, realCostTable):
    cost = 0
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            cost += realCostTable[y][x]
    return cost


def Insert(x1, y1, x2, y2, previsionTable, totalcost):
    insertLevel = (abs(x1-x2)+1)*(abs(y1-y2)+1)
    print("Insert Level:" + str(insertLevel))
    knownCost = 0
    nElemetLessPrecise = 0
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            if(previsionTable[y][x].level < insertLevel):
                knownCost += previsionTable[y][x].cost
            else:
                nElemetLessPrecise += 1

    costToSplit = totalcost - knownCost
    eachCost = costToSplit/nElemetLessPrecise

    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            if(previsionTable[y][x].level > insertLevel):
                previsionTable[y][x].cost = eachCost
                previsionTable[y][x].level = insertLevel


def calculateAndInsert(x1, y1, x2, y2, realCostTable, previsionTable):
    cost = SimulateRayTracer(x1, y1, x2, y2, realCostTable)
    estimate = estimatecost(x1, y1, x2, y2, previsionTable)
    print("x1:" + str(x1) + " " + "y1:" + str(y1) + " " + "x2:" + str(x2) + " " + "y2:" + str(y2) + "\n " +
          "prevision: " + str(estimate) + "  cost:"+str(cost))
    Insert(x1, y1, x2, y2, previsionTable, cost)


XSIZE=10
YSIZE=10


previsionTable = createTable(XSIZE, YSIZE)
realTable = createRandomTable(XSIZE, YSIZE)

printTablesBySide(previsionTable,realTable)

calculateAndInsert(0,0,XSIZE-1,YSIZE-1,realTable,previsionTable)
printTablesBySide(previsionTable,realTable)

calculateAndInsert(0,0,0,5,realTable,previsionTable)
printTablesBySide(previsionTable,realTable)

calculateAndInsert(0,0,0,0,realTable,previsionTable)
printTablesBySide(previsionTable,realTable)

