from algorithm.AlgorithmSimple import *
from algorithm.AlgorithmComplex import *


def calculateTrace2types(vxsize, vysize, samplesize, numberounds,realTable,namestr=""):
    previsionTableSimple = createTable(vxsize, vysize)
    previsionTableComplex = createTableC(vxsize, vysize)
    groups = InitiateGroups(previsionTableComplex)
    statisticsSimple = []
    statisticsComplex = []
    xplot = []

    for x in range(0,numberounds):
        inp = randomImput(vxsize,vysize)
        calculateAndInsertWithouPrintC(inp[0], inp[1], inp[2], inp[3], realTable, previsionTableComplex,groups)
        precision1 = getStatisticsC(realTable, previsionTableComplex, vxsize, vysize, samplesize)
        statisticsComplex.append(precision1)

        calculateAndInsertWithouPrint(inp[0], inp[1], inp[2], inp[3], realTable, previsionTableSimple)
        precision2 = getStatistics(realTable, previsionTableSimple, vxsize, vysize, samplesize)
        statisticsSimple.append(precision2)

        xplot.append(x)
    averagePrecisionComplex =np.average(statisticsComplex)
    averagePrecisionSimple = np.average(statisticsSimple)

    nameComplex = "Complex "+ namestr +" table size:"+str(vxsize)+"x"+str(vysize)+"  average precision:" +\
                  str(averagePrecisionComplex) + "%\n"
    traceComplex = go.Scatter(x=xplot, y=statisticsComplex,name =nameComplex)

    nameSimple = "Simple " + namestr + " table size:" + str(vxsize) + "x" + str(vysize) + "  average precision:" + \
                  str(averagePrecisionSimple) + "%\n"
    traceSimple = go.Scatter(x=xplot, y=statisticsSimple, name=nameSimple)

    traces = [traceComplex,traceSimple]
    return traces


def calculatePlotsOnImage2types(vxsize, vysize, fname, samplesize, numberounds):
    realTable = print_image(fname, (100, 100), (vxsize, vysize))
    print("got file table:" + fname)
    trace = calculateTrace2types(vxsize, vysize, samplesize, numberounds, realTable, fname)
    return trace


def calculatePlotsOnImageList2types(vxsize, vysize, filesname, samplesize, numberounds, times, growquocient):
    data = []
    for fname in filesname:
        trace = calculatePlotsOnImage2types(vxsize, vysize, fname, samplesize, numberounds)
        data.append(trace[0])
        data.append(trace[1])

    title = "Precision by the number of requests changin window size.\n" + \
            "   sample size:" + str(samplesize) + "\n"
    plotly.offline.plot({
        "data": data,
        "layout": Layout(title=title)
    })

def plotAlgorithm2types(vxsize, vysize, maxcost, samplesize, numberounds,name):
    realTable = createRandomTable(vxsize, vysize, maxcost)
    trace = calculateTrace2types(vxsize, vysize, samplesize, numberounds, realTable, name)
    return trace


def calculateSeveralPlots2types(vxsize, vysize, maxcost, samplesize, numberounds, times, growquocient):
    data = []
    for i in range(1, times+1):
        val = growquocient*i
        #data.append(plotAlgorithm(vxsize*val, vysize*val, maxcost, samplesize, numberounds))
        result = plotAlgorithm2types(vxsize*val, vysize*val, maxcost, samplesize, numberounds,str(i))
        data.append(result[0])
        data.append(result[1])

    title = "Precision by the number of requests changin window size.\n" + \
            "   sample size:" + str(samplesize) + "\n"
    plotly.offline.plot({
        "data": data,
        "layout": Layout(title=title)
    })





XSIZE=10
YSIZE=10
MAXCOST = 5000
SAMPLESIZE = 50
NUMBERROUNDS = 20

NTRACES = 5
GROWQUOCIENT =2

files =['test01.txt','test02.txt','test03.txt','test04.txt','test05.txt']
#calculatePlotsOnImageList2types(XSIZE, YSIZE, files, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)

calculateSeveralPlots2types(XSIZE, YSIZE, MAXCOST, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)