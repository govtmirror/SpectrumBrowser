#! /usr/local/bin/python2.7
# -*- coding: utf-8 -*-
#
#This software was developed by employees of the National Institute of
#Standards and Technology (NIST), and others. 
#This software has been contributed to the public domain. 
#Pursuant to title 15 Untied States Code Section 105, works of NIST
#employees are not subject to copyright protection in the United States
#and are considered to be in the public domain. 
#As a result, a formal license is not needed to use this software.
# 
#This software is provided "AS IS."  
#NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
#OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
#MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
#AND DATA ACCURACY.  NIST does not warrant or make any representations
#regarding the use of the software or the results thereof, including but
#not limited to the correctness, accuracy, reliability or usefulness of
#this software.


import DbCollections
import SensorDb
from Defines import SENSOR_ID
from Defines import STATUS


def getCaptureEvents():
    retval = {}
    captureEventCount = {}
    for sensorId in SensorDb.getAllSensorIds():
        captureEvent = DbCollections.getCaptureDb().find({SENSOR_ID: sensorId})
        if captureEvent is None:
            captureEventCount[sensorId] = 0
        else:
            captureEventCount[sensorId] = captureEvent.count()
    retval[STATUS] = "OK"
    retval["captureEventCount"] = captureEventCount
    return retval
