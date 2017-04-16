from algorithm.AlgorithmSimple import *



def calculateTrace2types(vxsize, vysize, samplesize, numberounds,realTable,namestr=""):
    allSimple =[]
    for Y in range(0, samplesize):
        previsionTableSimple = createTable(vxsize, vysize)
        statisticsSimple = []
        xplot = []

        for x in range(0,numberounds):
            inp = randomImput(vxsize,vysize)
            calculateAndInsertWithouPrint(inp[0], inp[1], inp[2], inp[3], realTable, previsionTableSimple)
            precision2 = getStatistics(realTable, previsionTableSimple, vxsize, vysize, samplesize)
            statisticsSimple.append(precision2)
        allSimple.append(statisticsSimple)

    statisticsSimple = []


    for x in range(0, numberounds):
        total = 0
        for l in allSimple:
            total +=l[x]
        statisticsSimple.append(total/len(allSimple))

    print(statisticsSimple)

    for x in range(0, numberounds):
        xplot.append(x)

    averagePrecisionSimple = np.average(statisticsSimple)

    nameSimple = namestr + " " + str(vxsize) + "x" + str(vysize) + "  average precision:" + \
                  str(averagePrecisionSimple) + "%\n"
    traceSimple = go.Scatter(x=xplot, y=statisticsSimple, name=nameSimple)

    traces = [traceSimple]
    return traces


def calculatePlotsOnImage2types(vxsize, vysize, realTable, samplesize, numberounds):
    trace = calculateTrace2types(vxsize, vysize, samplesize, numberounds, realTable, fname)
    return trace


def calculatePlotsOnImageList2types(vxsize, vysize, filesname, samplesize, numberounds, times, growquocient):
    data = []
    for fname in filesname:
        for i in range(1, times + 1):
            val = growquocient * i
            realTable = print_image(fname, (100, 100), (vxsize*val, vysize*val))
            print("got file table:" + fname+" Output"+str(vxsize*val)+"x"+str(vysize*val))
            trace = calculateTrace2types(vxsize*val, vysize*val, samplesize, numberounds, realTable, fname)
            data.append(trace[0])

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

        title = "Precision by the number of requests changin window size.\n" + \
                "   sample size:" + str(samplesize) + "\n"
        plotly.offline.plot({
            "data": data,
            "layout": Layout(title=title)
        })





XSIZE=10
YSIZE=10
MAXCOST = 5000
SAMPLESIZE = 40
NUMBERROUNDS = 20

NTRACES = 5
GROWQUOCIENT =1

files =['test01.txt','test02.txt','test03.txt','test04.txt','test05.txt']
calculatePlotsOnImageList2types(XSIZE, YSIZE, files, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)

#calculateSeveralPlots2types(XSIZE, YSIZE, MAXCOST, SAMPLESIZE, NUMBERROUNDS,NTRACES,GROWQUOCIENT)