import flaskr as main
from flask import request
from flask import jsonify
import random
import util
import socket
import Config
import time
import threading
import Accounts
from __builtin__ import True
TWO_HOURS = 2*60*60
SIXTY_DAYS = 60*60*60*60
accountLock = threading.Lock()
sessionLock = threading.Lock()
sessions = main.admindb.sessions
accounts = main.admindb.accounts


def checkSessionId(sessionId):
 
    sessionFound = False
    if main.debug :
        sessionFound = True
    else:
        sessionLock.acquire() 
        try :
            session =sessions.find_one({"sessionId":sessionId})
            if session <> None:
                sessionFound = True
                session["expireTime"] = time.time()+TWO_HOURS
                sessions.update({"_id":session["_id"]},{"$set":session},upsert=False)
                util.debugPrint("updated session ID expireTime")
        except:
            util.debugPrint("Problem checking sessionKey " + sessionId)
        finally:
            sessionLock.release()  
    return sessionFound

# Place holder. We need to look up the database for whether or not this is a valid sensor key.
def authenticateSensor(sensorId, sensorKey):
    return True

def logOut(sessionId):
    sessionLock.acquire() 
    logOutSuccessful = False
    try :
        util.debugPrint("Logging off " + sessionId)
        session = sessions.find_one({"sessionId":sessionId}) 
        if session == None:
            util.debugPrint("When logging off could not find the following session key to delete:" + sessionId)
        else:
            sessions.remove({"_id":session["_id"]})
            logOutSuccessful = True
    except:
        util.debugPrint("Problem logging off " + sessionId)
    finally:
        sessionLock.release() 
    return logOutSuccessful

def authenticatePeer(peerServerId,password):
    peerRecord = Config.findInboundPeer(peerServerId)
    if peerRecord == None:
        return False
    else:
        return password == peerRecord["key"]

def generateGuestToken():
    sessionId = generateSessionKey("user")
    addedSuccessfully = addSessionKey(sessionId, "guest")
    return sessionId

def generatePeerSessionKey():
    sessionId = generateSessionKey("peer")
    addSessionKey(sessionId, "peerUser")

def generateSessionKey(privilege):
    util.debugPrint("generateSessionKey ")
    try:
        sessionId = -1
        uniqueSessionId = False
        num = 0
        sessionLock.acquire()
        # try 5 times to get a unique session id
        while (not uniqueSessionId) and (num<5):
            #JEK: I used time.time() as my random number so that if a user wants to create
            # sessions from 2 browsers on the same machine, the time should ensure uniqueness
            # especially since time goes down to msecs.
            #JEK I am thinking that we do not need to add remote_address to the sessionId to get uniqueness,
            # so I took out +request.remote_addr
            sessionId = privilege + "-" + "{0:.6f}".format(time.time())+str(random.randint(1,100000))
            util.debugPrint("SessionKey in loop = "+str(sessionId))            
            session = sessions.find_one({"sessionId":sessionId}) 
            if session == None:
                uniqueSessionId = True       
            else:
                num = num+1
        if num == 5:
            util.debugPrint("Fix unique session key generation code. We tried 5 times to get a unique session key and then we gave up.") 
            sessionId = -1
    except:     
        util.debugPrint("Problem generating sessionKey " + str(sessionId))  
        sessionId = -1
    finally:
        sessionLock.release() 
    util.debugPrint("SessionKey = "+str(sessionId))      
    return sessionId   

def addSessionKey(sessionId, userName):
    util.debugPrint("addSessionKey")
    if sessionId <> -1:
        sessionLock.acquire()
        try :
            session = sessions.find_one({"sessionId":sessionId}) 
            if session == None:
                newSession = {"sessionId":sessionId,"userName":userName,"timeLogin":time.time(),"expireTime":time.time()+TWO_HOURS}
                sessions.insert(newSession)
                return True
            else:
                util.debugPrint("session key already exists, we should never reach since only should generate unique session keys")
                return False
        except:
            util.debugPrint("Problem adding sessionKey " + sessionId)
            return False
        finally:
            sessionLock.release()      
    else:
        return False
 
def IsAccountLocked(userName):
    AccountLocked = False
    if Config.isAuthenticationRequired():
        accountLock.acquire()
        try :
            existingAccount= accounts.find_one({"emailAddress":userName})    
            if existingAccount <> None:               
                if existingAccount["accountLocked"] == "True":
                    AccountLocked = True
        except:
            util.debugPrint("Problem authenticating user " + userName + " password: "+ password)
        finally:
            accountLock.release()   
    return AccountLocked
    
def authenticate(userName, password):
    print userName,password, Config.isAuthenticationRequired()
    authenicationSuccessful = False
    util.debugPrint("authenticate check database")
    if Config.isAuthenticationRequired():
        accountLock.acquire()
        try :
            util.debugPrint("finding existing account")
            existingAccount = accounts.find_one({"emailAddress":userName, "password":password})

            if existingAccount == None:
                util.debugPrint("did not find email and password ") 
                existingAccount= accounts.find_one({"emailAddress":userName})    
                if existingAccount <> None:
                    util.debugPrint("account exists, but user entered wrong password")                    
                    numFailedLoggingAttempts = existingAccount["numFailedLoggingAttempts"] +1
                    existingAccount["numFailedLoggingAttempts"] = numFailedLoggingAttempts
                    if numFailedLoggingAttempts == 5:                 
                        existingAccount["accountLocked"] = "True" 
                    accounts.update({"_id":existingAccount["_id"]},{"$set":existingAccount},upsert=False)                           
            else:
                util.debugPrint("found email and password ") 
                if existingAccount["accountLocked"] == "False":
                    util.debugPrint("user passed login authentication.")           
                    existingAccount["numFailedLoggingAttempts"] = 0
                    existingAccount["accountLocked"] = "False" 
                    # Place-holder. We need to access LDAP (or whatever) here.
                    accounts.update({"_id":existingAccount["_id"]},{"$set":existingAccount},upsert=False)
                    util.debugPrint("user login info updated.")
                    authenicationSuccessful = True
        except:
            util.debugPrint("Problem authenticating user " + userName + " password: "+ password)
        finally:
            accountLock.release()    
    else:
        if userName == "admin":
            if password == Config.getAdminPassword():
                authenicationSuccessful = True
        else:
            authenicationSuccessful = True    
    return authenicationSuccessful

def authenticateUser(privilege, userName, password):
    """
     Authenticate a user given a requested privilege, userName and password.
    """
    util.debugPrint("authenticateUser")
    util.debugPrint("authenticateUser: " + userName + " privilege: " + privilege + " password " + password)
    if privilege == "admin" or privilege == "user":
       if IsAccountLocked(userName):
           return jsonify({"status":"ACCLOCKED", "sessionId":"0"})
       else:
           # Authenticate will will work whether passwords are required or not (authenticate = true if no pwd req'd)
           if authenticate(userName, password) :
               sessionId = generateSessionKey(privilege)
               addedSuccessfully = addSessionKey(sessionId, userName)
               if addedSuccessfully:
                   return jsonify({"status":"OK", "sessionId":sessionId})
               else:
                   return jsonify({"status":"INVALSESSION", "sessionId":"0"})
           else:
               util.debugPrint("invalid user will be returned: ")
               return jsonify({"status":"INVALUSER", "sessionId":"0"})
# JEK: I think we authenticate peers via the authenticatePeer function.
#        elif privilege == "peer":
#        if authenticate(userName, password, privilege):
#            sessionId = generatePeerSessionKey()
#            addSessionKey(request.remote_addr, sessionId)
#            return jsonify({"status":"OK", "sessionId":sessionId}), 200
#    else:
#        return jsonify({"status":"NOK", "sessionId":"0"}), 403         
    else:
       # q = urlparse.parse_qs(query,keep_blank_values=True)
       # TODO deal with actual logins consult user database etc.
       return jsonify({"status":"NOK", "sessionId":sessionId}), 401

# TODO -- this will be implemented after the admin stuff
# has been implemented.
def isUserRegistered(emailAddress):
    UserRegistered = False
    if Config.isAuthenticationRequired():
        accountLock.acquire()
        try :
            existingAccount = accounts.find_one({"emailAddress":emailAddress})
            if existingAccount <> None:
                UserRegistered = True
        except:
            util.debugPrint("Problem checking if user is registered " + userName )
        finally:
            accountLock.release()    

    return UserRegistered


Accounts.removeExpiredRows(sessions)



