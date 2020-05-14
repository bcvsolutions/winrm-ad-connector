#!/usr/bin/env python
# -*- coding: utf-8 -*-
# All params from IdM is stored in environment and you can get them by os.environ["paramName"]
import sys, os
# this is needed for importing file winrm_wrapper from parent dir
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import winrm_wrapper
import codecs

uid = winrm_wrapper.getParam("sAMAccountName")
homeDirectory = winrm_wrapper.getParam("homeDirectory")

winrm_wrapper.writeLog("Create start for " + uid)

# Load PS script from file and replace params
winrm_wrapper.writeLog("loading script")
f = codecs.open(os.environ["script"], encoding='utf-8', mode='r')
command = f.read()
command = command.replace("$uid", uid)
if homeDirectory is not None and homeDirectory != "":
    notUsedHomeDir = homeDirectory.replace(uid, "_" + uid)
    command = command.replace("$homeDirectory", homeDirectory)
    command = command.replace("$notUsedHomeDir", notUsedHomeDir)

    # Call wrapper
    winrm_wrapper.executeScript(os.environ["endpoint"], os.environ["authentication"], os.environ["user"],
                                	os.environ["password"], command, uid)

    winrm_wrapper.writeLog("Create end for " + uid)
else:
    winrm_wrapper.writeLog("Create end, home dir was not created")
    
print("__UID__=" + uid)
sys.exit()
