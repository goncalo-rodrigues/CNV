import plotly
from plotly.graph_objs import Scatter, Layout
import pprint
import random
import decimal
import math
from datetime import datetime
import time
import numpy as np
import plotly.graph_objs as go

from algorithm.imageLoader import print_image



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



#-----------------Project Functions------------------

#this is the info that we need about each element on the matrix
class TableElement:
    def __init__(self, cost, level, group):
        self.cost = cost        #the predicted cost to comput this elemet
        self.level = level      #the precision of its cost (less is better)
        self.group = group      #the lowest level grout to wich it pertences


# This is the query that gives an estimation about how much will cost
def estimatecost(x1, y1, x2, y2, previsionTable):
    prevision = 0
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            prevision += previsionTable[y][x].cost
    return prevision


# this is the method to be aplied after render in orde to improve prevision table
def Insert(x1, y1, x2, y2, previsionTable, totalcost):
    insertLevel = (abs(x1-x2)+1)*(abs(y1-y2)+1)
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


# -------------      END    ------------------------------

# Creates a prevision table
def createTable(Xsize, Ysize):
    totalpixel = Xsize*Ysize+1;
    table = [[TableElement(0,totalpixel,0) for x in range(Xsize)] for y in range(Ysize)]
    return table


#Create a completly random table
def createRandomTable(Xsize, Ysize, maxCost):
    r = random.SystemRandom()
    table = [[r.randint(1, maxCost) for x in range(Xsize)] for y in range(Ysize)]
    return table


# print the 2 tables side by side
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


# This method simulates the execution of the raytrace returning the cost
def SimulateRayTracer(x1, y1, x2, y2, realCostTable):
    cost = 0
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            cost += realCostTable[y][x]
    return cost


# Calculates the cost of render and update the prevision table
def calculateAndInsert(x1, y1, x2, y2, realCostTable, previsionTable):
    #time.sleep(0.1)
    cost = SimulateRayTracer(x1, y1, x2, y2, realCostTable)
    estimate = estimatecost(x1, y1, x2, y2, previsionTable)
    precision = resultPrecision(estimate,cost)
    insertLevel = (abs(x1 - x2) + 1) * (abs(y1 - y2) + 1)
    print("x1:" + str(x1) + " " + "y1:" + str(y1) + " " + "x2:" + str(x2) + " " + "y2:" + str(y2) + "\n " +
          "prevision: " + str(estimate) + "  cost:"+str(cost) + "  precision: "+
          str(precision)+"%\ninsert level:"+str(insertLevel))
    Insert(x1, y1, x2, y2, previsionTable, cost)


def calculateAndInsertWithouPrint(x1, y1, x2, y2, realCostTable, previsionTable):
    cost = SimulateRayTracer(x1, y1, x2, y2, realCostTable)
    Insert(x1, y1, x2, y2, previsionTable, cost)


# Runs samplesize times a random request over the current previsiontable state
# and check its accuaricy comparing to real cost table
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


# return the Preciso of the gess
def resultPrecision(v1, v2):
    # abs(esperado - real) / real
    if v1 == v2:
        return 100
    elif v1 ==0 or v2 == 0:
        return 0
    elif(v1 > v2):
        return v2 / v1 * 100
    else:
        return v1 / v2 * 100


# Emulates a completely random request
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


# Displays the evolution of the prevision table
def visualAlgorithm(vxsize,vysize,maxcost,samplesize,numberounds):
    previsionTable = createTable(vxsize, vysize)
    realTable = createRandomTable(vxsize, vysize,maxcost)
    printTablesBySide(previsionTable,realTable)
    statistics = []

    for x in range(0,numberounds):
        inp = randomImput(vxsize,vysize)
        print(inp[0], inp[1], inp[2], inp[3])
        calculateAndInsert(inp[0], inp[1], inp[2], inp[3], realTable, previsionTable)
        printTablesBySide(previsionTable, realTable)
        precision1 = getStatistics(realTable, previsionTable,vxsize,vysize,samplesize)
        print(precision1)
        statistics.append(precision1)

    print(statistics)


# create a trace with the evolution of precision by the number of server requests
def plotAlgorithm(vxsize, vysize, maxcost, samplesize, numberounds):
    previsionTable = createTable(vxsize, vysize)
    realTable = createRandomTable(vxsize, vysize, maxcost)
    statistics = []
    xplot = []

    for x in range(0,numberounds):
        inp = randomImput(vxsize,vysize)
        calculateAndInsertWithouPrint(inp[0], inp[1], inp[2], inp[3], realTable, previsionTable)
        precision1 = getStatistics(realTable, previsionTable, vxsize, vysize, samplesize)
        statistics.append(precision1)
        xplot.append(x)
    averagePrecision = np.average(statistics)

    name = "SIMPLE   table size:"+str(vxsize)+"x"+str(vysize)+"  average precision:" + str(averagePrecision) + "%\n"
    trace = go.Scatter(x=xplot, y=statistics,name =name)#,line=dict(shape='spline'))
    return trace


def calculate1plot(vxsize, vysize, maxcost, samplesize, numberounds):
    calculateSeveralPlots(vxsize, vysize, maxcost, samplesize, numberounds, 1, 1)


def calculateSeveralPlots(vxsize, vysize, maxcost, samplesize, numberounds, times, growquocient):
    data = []
    for i in range(1,times+1):
        val = growquocient*i
        #data.append(plotAlgorithm(vxsize*val, vysize*val, maxcost, samplesize, numberounds))
        data.append(plotAlgorithm(vxsize, vysize, maxcost*val, samplesize, numberounds))

    title = "Precision by the number of requests changin window size.\n" + \
            "   sample size:" + str(samplesize) + "\n"
    plotly.offline.plot({
        "data": data,
        "layout": Layout(title=title)
    })



def calculatePlotsOnImages(vxsize, vysize, filesname, samplesize, numberounds, times, growquocient):
    for fname in filesname:
        previsionTable = createTable(vxsize, vysize)
        realTable = print_image(fname, resolution=(100,100), grid=(vxsize,vysize))
        print("got file table:" + fname)
        statistics = []
        xplot = []
        for x in range(0, numberounds):
            inp = randomImput(vxsize, vysize)
            calculateAndInsertWithouPrint(inp[0], inp[1], inp[2], inp[3], realTable, previsionTable)
            precision1 = getStatistics(realTable, previsionTable, vxsize, vysize, samplesize)
            statistics.append(precision1)
            xplot.append(x)
        averagePrecision = np.average(statistics)

        name = "SIMPLE"+ fname+"  table size:" + str(vxsize) + "x" + str(vysize) + "  average precision:" + str(
            averagePrecision) + "%\n"
        trace = go.Scatter(x=xplot, y=statistics, name=name)  # ,line=dict(shape='spline'))
        data = [trace]
        title = "Precision by the number of requests changin window size.\n" + \
                "   sample size:" + str(samplesize) + "\n"
        plotly.offline.plot({
            "data": data,
            "layout": Layout(title=title)
        })


XSIZE=10
YSIZE=10
MAXCOST = 10
SAMPLESIZE = 50
NUMBERROUNDS = 20

NTRACES = 10
GROWQUOCIENT =1

files =['test01.txt','test02.txt','test03.txt','test04.txt','test05.txt']

#visualAlgorithm(XSIZE,YSIZE,MAXCOST,SAMPLESIZE,NUMBERROUNDS)
#calculateSeveralPlots(XSIZE, YSIZE, MAXCOST, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)
calculatePlotsOnImages(XSIZE, YSIZE, files, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)

"""
steps = []
for i in range(len(data)):
    step = dict(
        method = 'restyle',
        args = ['visible', [False] * len(data)],
    )
    step['args'][1][i] = True # Toggle i'th trace to "visible"
    steps.append(step)

sliders = [dict(active = 10,currentvalue = {"prefix": "Frequency: "}, pad = {"t": 50}, steps = steps )]

layout = dict(sliders=sliders)

fig = dict(data=data, layout=layout)

py.iplot(fig, filename='Sine Wave Slider')
"""
