from algorithm.ComonFunctions import *


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
    lessPrecise = []
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            if(previsionTable[y][x].level < insertLevel):
                knownCost += previsionTable[y][x].cost
            else:
                nElemetLessPrecise += 1
                lessPrecise.append(previsionTable[y][x])

    costToSplit = totalcost - knownCost
    if costToSplit <0:
        costToSplit = nElemetLessPrecise

    if nElemetLessPrecise >0:
        eachCost = costToSplit/nElemetLessPrecise
        for e in lessPrecise:
            e.cost = eachCost
            e.level = insertLevel


# Creates a prevision table
def createTable(Xsize, Ysize):
    totalpixel = Xsize*Ysize+1;
    table = [[TableElement(0,totalpixel,0) for x in range(Xsize)] for y in range(Ysize)]
    return table


# -------------      END    ------------------------------


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





#================================================================
#---            from here are the ways to 'PLAY'              ---
#================================================================

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


def calculateTrace(vxsize, vysize, samplesize, numberounds,realTable,namestr=""):
    previsionTable = createTable(vxsize, vysize)
    statistics = []
    xplot = []

    for x in range(0,numberounds):
        inp = randomImput(vxsize,vysize)
        calculateAndInsertWithouPrint(inp[0], inp[1], inp[2], inp[3], realTable, previsionTable)
        precision1 = getStatistics(realTable, previsionTable, vxsize, vysize, samplesize)
        statistics.append(precision1)
        xplot.append(x)
    averagePrecision = np.average(statistics)

    name = "SIMPLE "+ namestr +" table size:"+str(vxsize)+"x"+str(vysize)+"  average precision:" + str(averagePrecision) + "%\n"
    trace = go.Scatter(x=xplot, y=statistics,name =name)#,line=dict(shape='spline'))
    return trace


# create a trace with the evolution of precision by the number of server requests
def plotAlgorithm(vxsize, vysize, maxcost, samplesize, numberounds):
    previsionTable = createTable(vxsize, vysize)
    realTable = createRandomTable(vxsize, vysize, maxcost)
    return calculateTrace(vxsize, vysize, samplesize, numberounds,realTable)




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

def calculatePlotsOnImage(vxsize, vysize, fname, samplesize, numberounds):
    previsionTable = createTable(vxsize, vysize)
    realTable = print_image(fname, (100, 100), (vxsize, vysize))
    print("got file table:" + fname)
    trace = calculateTrace(vxsize, vysize, samplesize, numberounds, realTable, fname)
    return trace


def calculatePlotsOnImageList(vxsize, vysize, filesname, samplesize, numberounds, times, growquocient):
    data = []
    for fname in filesname:
        trace = calculatePlotsOnImage(vxsize, vysize, fname, samplesize, numberounds)
        data.append(trace)
        #t = threading.Thread(target=calculatePlotsOnImage, args=(vxsize, vysize, fname, samplesize, numberounds))
        #t.start()
    title = "Precision by the number of requests changin window size.\n" + \
            "   sample size:" + str(samplesize) + "\n"
    plotly.offline.plot({
        "data": data,
        "layout": Layout(title=title)
    })


'''
XSIZE=10
YSIZE=10
MAXCOST = 10
SAMPLESIZE = 50
NUMBERROUNDS = 20
NTRACES = 10
GROWQUOCIENT =1
files =['test01.txt','test02.txt','test03.txt','test04.txt','test05.txt']
#visualAlgorithm(XSIZE,YSIZE,MAXCOST,SAMPLESIZE,NUMBERROUNDS)
calculateSeveralPlots(XSIZE, YSIZE, MAXCOST, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)
#calculatePlotsOnImageList(XSIZE, YSIZE, files, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)
'''

