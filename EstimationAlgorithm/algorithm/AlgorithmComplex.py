from algorithm.ComonFunctions import *


#-----------------Project Functions------------------


class TableElementC:
    def __init__(self, cost, level, group, x, y):
        self.cost = cost        # the predicted cost to comput this elemet
        self.level = level      # the precision of its cost (less is better)
        self.group = group      # the lowest level grout to wich it pertences
        self.x = x
        self.y = y


class Group:
    groupId = 0
    cost = 0
    level = 0
    nElements = 0
    members = []

    def __init__(self, groupId):
        self.groupId = groupId

    def addElement(self,element):
        self.members.append(element)
        self.cost += element.cost
        self.level += 1
        self.nElements += 1


    def removeElememt(self,elements):
        for e in elements:
            self.members.remove(e)
            self.cost -= e.cost
            self.level -= 1
            self.nElements -= 1

        if self.groupId != 0 & self.nElements != 0:
            newCost = self.cost/self.nElements
            for e in self.members:
                e.cost = newCost
                e.level = self.level


# Creates a prevision table
def createTableC(Xsize, Ysize):
    totalpixel = Xsize * Ysize + 1;
    table = [[TableElementC(0, totalpixel, 0, x, y) for x in range(Xsize)] for y in range(Ysize)]
    return table

def InitiateGroups(previsionTable):
    group = Group(0)
    groups = {}
    for line in previsionTable:
        for row in line:
            group.addElement(row)
    groups[0] = group
    return groups


# This is the query that gives an estimation about how much will cost
def estimatecostC(x1, y1, x2, y2, previsionTable):
    prevision = 0
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            prevision += previsionTable[y][x].cost
    return prevision


# this is the method to be aplied after render in orde to improve prevision table
def InsertC(x1, y1, x2, y2, previsionTable, totalcost,groups):
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
        newGroup = Group(len(groups))
        groups[newGroup.groupId] = newGroup
        eachCost = costToSplit/nElemetLessPrecise
        for e in lessPrecise:
               e.cost = eachCost
               e.level = insertLevel
               groups[e.group].removeElememt([e])
               e.group = newGroup.groupId
               newGroup.addElement(e)
               # todo use an array of lessprecise instead




# -------------      END    ------------------------------

#----------EQUAL in complex and simple -------------------


# Runs samplesize times a random request over the current previsiontable state
# and check its accuaricy comparing to real cost table
def getStatisticsC(realCostTable, previsionTable,xsize,ysize,sampleSize):
    total = 0
    n = 0
    for x in range(0,sampleSize):
        window = randomImput(xsize, ysize)
        cost = SimulateRayTracer(window[0], window[1], window[2], window[3], realCostTable)
        estimate = estimatecostC(window[0], window[1], window[2], window[3], previsionTable)
        precision = resultPrecision(estimate, cost)
        n += 1
        total += precision

    return total/n


#------------------------ difrent -----------------------------



def calculateAndInsertWithouPrintC(x1, y1, x2, y2, realCostTable, previsionTable, groups):
    cost = SimulateRayTracer(x1, y1, x2, y2, realCostTable)
    InsertC(x1, y1, x2, y2, previsionTable, cost, groups)


# Calculates the cost of render and update the prevision table
def calculateAndInsertC(x1, y1, x2, y2, realCostTable, previsionTable):
    #time.sleep(0.1)
    cost = SimulateRayTracer(x1, y1, x2, y2, realCostTable)
    estimate = estimatecostC(x1, y1, x2, y2, previsionTable)
    precision = resultPrecision(estimate,cost)
    insertLevel = (abs(x1 - x2) + 1) * (abs(y1 - y2) + 1)
    print("x1:" + str(x1) + " " + "y1:" + str(y1) + " " + "x2:" + str(x2) + " " + "y2:" + str(y2) + "\n " +
          "prevision: " + str(estimate) + "  cost:"+str(cost) + "  precision: "+
          str(precision)+"%\ninsert level:"+str(insertLevel))
    InsertC(x1, y1, x2, y2, previsionTable, cost)



#================================================================
#---            from here are the ways to 'PLAY'              ---
#================================================================

# Displays the evolution of the prevision table
def visualAlgorithmC(vxsize,vysize,maxcost,samplesize,numberounds):
    previsionTable = createTableC(vxsize, vysize)
    groups = InitiateGroups(previsionTable)
    realTable = createRandomTable(vxsize, vysize,maxcost)
    printTablesBySide(previsionTable,realTable)
    statistics = []

    for x in range(0,numberounds):
        inp = randomImput(vxsize,vysize)
        print(inp[0], inp[1], inp[2], inp[3])
        calculateAndInsertC(inp[0], inp[1], inp[2], inp[3], realTable, previsionTable, groups)
        printTablesBySide(previsionTable, realTable)
        precision1 = getStatisticsC(realTable, previsionTable,vxsize,vysize,samplesize)
        print(precision1)
        statistics.append(precision1)

    print(statistics)


def calculateTraceC(vxsize, vysize, samplesize, numberounds,realTable,namestr=""):
    previsionTable = createTableC(vxsize, vysize)
    groups = InitiateGroups(previsionTable)
    statistics = []
    xplot = []

    for x in range(0,numberounds):
        inp = randomImput(vxsize,vysize)
        calculateAndInsertWithouPrintC(inp[0], inp[1], inp[2], inp[3], realTable, previsionTable,groups)
        precision1 = getStatisticsC(realTable, previsionTable, vxsize, vysize, samplesize)
        statistics.append(precision1)
        xplot.append(x)
    averagePrecision = np.average(statistics)

    name = "Complex "+ namestr +" table size:"+str(vxsize)+"x"+str(vysize)+"  average precision:" + str(averagePrecision) + "%\n"
    trace = go.Scatter(x=xplot, y=statistics,name =name)#,line=dict(shape='spline'))
    return trace


# create a trace with the evolution of precision by the number of server requests
def plotAlgorithmC(vxsize, vysize, maxcost, samplesize, numberounds):
    previsionTable = createTableC(vxsize, vysize)
    realTable = createRandomTable(vxsize, vysize, maxcost)
    return calculateTraceC(vxsize, vysize, samplesize, numberounds,realTable)




def calculate1plotC(vxsize, vysize, maxcost, samplesize, numberounds):
    calculateSeveralPlotsC(vxsize, vysize, maxcost, samplesize, numberounds, 1, 1)


def calculateSeveralPlotsC(vxsize, vysize, maxcost, samplesize, numberounds, times, growquocient):
    data = []
    for i in range(1,times+1):
        val = growquocient*i
        #data.append(plotAlgorithm(vxsize*val, vysize*val, maxcost, samplesize, numberounds))
        data.append(plotAlgorithmC(vxsize*val, vysize*val, maxcost, samplesize, numberounds))

    title = "Precision by the number of requests changin window size.\n" + \
            "   sample size:" + str(samplesize) + "\n"
    plotly.offline.plot({
        "data": data,
        "layout": Layout(title=title)
    })

def calculatePlotsOnImageC(vxsize, vysize, fname, samplesize, numberounds):
    previsionTable = createTableC(vxsize, vysize)
    realTable = print_image(fname, (100, 100), (vxsize, vysize))
    print("got file table:" + fname)
    trace = calculateTraceC(vxsize, vysize, samplesize, numberounds, realTable, fname)
    return trace


def calculatePlotsOnImageListC(vxsize, vysize, filesname, samplesize, numberounds, times, growquocient):
    data = []
    for fname in filesname:
        trace = calculatePlotsOnImageC(vxsize, vysize, fname, samplesize, numberounds)
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
SAMPLESIZE = 40
NUMBERROUNDS = 20

NTRACES = 6
GROWQUOCIENT =1

files =['test01.txt','test02.txt','test03.txt','test04.txt','test05.txt']

#visualAlgorithm(XSIZE,YSIZE,MAXCOST,SAMPLESIZE,NUMBERROUNDS)
calculateSeveralPlotsC(XSIZE, YSIZE, MAXCOST, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)
#calculatePlotsOnImageListC(XSIZE, YSIZE, files, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)

'''