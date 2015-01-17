import flaskr as main
from flask import jsonify
import os


def getPath(x):
    if main.launchedFromMain:
        return x
    else:
        return main.flaskRoot + x

def debugPrint(string):
    if main.debug :
        print string

def roundTo1DecimalPlaces(value):
    newVal = int((value+0.05)*10)
    return float(newVal)/float(10)

def roundTo2DecimalPlaces(value):
    newVal = int((value+0.005)*100)
    return float(newVal)/float(100)

def roundTo3DecimalPlaces(value):
    newVal = int((value+.0005)*1000)
    return float(newVal)/float(1000)

def load_symbol_map(symbolMapDir):
    files = [ symbolMapDir + f for f in os.listdir(symbolMapDir) if os.path.isfile(symbolMapDir + f) and os.path.splitext(f)[1] == ".symbolMap" ]
    if len(files) != 0:
        symbolMap = files[0]
        symbolMapFile = open(symbolMap)
        lines = symbolMapFile.readlines()
        for line in lines:
            if line[0] == "#":
                continue
            else:
                pieces = line.split(',')
                lineNo = pieces[-2]
                fileName = pieces[-3]
                symbol = pieces[0]
                main.gwtSymbolMap[symbol] = {"file":fileName, "line" : lineNo}

def loadGwtSymbolMap():
    symbolMapDir = getPath("static/WEB-INF/deploy/spectrumbrowser/symbolMaps/")
    load_symbol_map(symbolMapDir)
    symbolMapDir = getPath("static/WEB-INF/deploy/admin/symbolMaps/")
    load_symbol_map(symbolMapDir)

def formatError(errorStr):
    return jsonify({"Error": errorStr})

def decodeStackTrace (stackTrace):
    lines = stackTrace.split()
    for line in lines :
        pieces = line.split(":")
        if pieces[0] in main.gwtSymbolMap :
            print main.gwtSymbolMap.get(pieces[0])
            file = main.gwtSymbolMap.get(pieces[0])["file"]
            lineNo = main.gwtSymbolMap.get(pieces[0])["line"]
            print file, lineNo,pieces[1]
            
def getMySensorIds():
    """
    get a collection of sensor IDs that we manage.
    """
    sensorIds = set()
    systemMessages = main.getSystemMessages().find()
    for systemMessage in systemMessages:
        sid = systemMessage[main.SENSOR_ID]
        sensorIds.add(sid)
    return sensorIds

def generateUrl(protocol,host,port):
    return protocol+ "://" + host + ":" + str(port)
