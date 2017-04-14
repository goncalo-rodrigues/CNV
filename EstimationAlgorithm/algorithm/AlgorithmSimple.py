import pprint
import random
import decimal
import math
from datetime import datetime
import time

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


def createRandomTable(Xsize, Ysize, maxCost):
    r = random.SystemRandom()
    table = [[r.randint(1, maxCost) for x in range(Xsize)] for y in range(Ysize)]
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
    if costToSplit <0:
        costToSplit = nElemetLessPrecise

    if nElemetLessPrecise >0:
        eachCost = costToSplit/nElemetLessPrecise
        for x in range(x1, x2+1):
            for y in range(y1, y2+1):
                if(previsionTable[y][x].level > insertLevel):
                    previsionTable[y][x].cost = eachCost
                    previsionTable[y][x].level = insertLevel


def calculateAndInsert(x1, y1, x2, y2, realCostTable, previsionTable):
    #time.sleep(0.1)
    cost = SimulateRayTracer(x1, y1, x2, y2, realCostTable)
    estimate = estimatecost(x1, y1, x2, y2, previsionTable)
    precision = resultPrecision(estimate,cost)
    print("x1:" + str(x1) + " " + "y1:" + str(y1) + " " + "x2:" + str(x2) + " " + "y2:" + str(y2) + "\n " +
          "prevision: " + str(estimate) + "  cost:"+str(cost) + "  precision: "+
          str(precision)+"%")
    Insert(x1, y1, x2, y2, previsionTable, cost)



def getStatistics(realCostTable, previsionTable,xsize,ysize,sampleSize):
    total = 0
    n = 0
    for x in range(0,sampleSize):
        window = randomImput(xsize, ysize)
        cost = SimulateRayTracer(window[0], window[1], window[2], window[3], realCostTable)
        estimate = estimatecost(window[0], window[1], window[2], window[3], previsionTable)
        precision = resultPrecision(estimate, cost)
        n += 1
        total += precision

    return total/n

def resultPrecision(v1,v2):
    # abs(esperado - real) / real
    if v1 == v2:
        return 100
    elif v1 ==0 or v2 == 0:
        return 0
    elif(v1 > v2):
        return v2 / v1 * 100
    else:
        return v1 / v2 * 100


def randomImput(sizex, sizey):
    r = random.SystemRandom()
    xs = r.randint(1, sizex)
    ys = r.randint(1, sizey)
    x1 = r.randint(0, sizex - xs)
    x2 = x1+xs-1
    y1 = r.randint(0, sizey - ys)
    y2 = y1+ys-1
    result = (x1, y1, x2, y2)
    return result

XSIZE=10
YSIZE=10
MAXCOST = 9
SAMPLESIZE = 10
NUMBERROUNDS = 10

def visualAlgorithm():
    previsionTable = createTable(XSIZE, YSIZE)
    realTable = createRandomTable(XSIZE, YSIZE,MAXCOST)
    printTablesBySide(previsionTable,realTable)
    statistics = []
    x = []

    for x in range(0,NUMBERROUNDS):
        inp = randomImput(XSIZE,YSIZE)
        print(inp[0], inp[1], inp[2], inp[3])
        calculateAndInsert(inp[0], inp[1], inp[2], inp[3], realTable, previsionTable)
        printTablesBySide(previsionTable, realTable)
        precision1 = getStatistics(realTable, previsionTable,XSIZE,YSIZE,SAMPLESIZE)
        print(precision1)
        statistics.append(precision1)

    print(statistics)

visualAlgorithm()

