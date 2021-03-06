#!/usr/bin/env python
# -*- coding: utf-8 -*-
# All params from IdM is stored in environment and you can get them by os.environ["paramName"]
import sys, os
# this is needed for importing file winrm_wrapper from parent dir
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import winrm_wrapper
import codecs

uid = winrm_wrapper.getParam("__UID__")

if not uid:
    winrm_wrapper.writeLog("Search for ALL started")
else:
    winrm_wrapper.writeLog("Search start for " + uid)

# Load PS script from file and replace params
winrm_wrapper.writeLog("loading script")
f = codecs.open(os.environ["script"], encoding='utf-8', mode='r')
command = f.read()
command = command.replace("$uid", uid)

# Call wrapper
winrm_wrapper.executeScript(os.environ["endpoint"], os.environ["authentication"], os.environ["user"],
                                    os.environ["password"], os.environ["caTrustPath"], os.environ["ignoreCaValidation"], command, uid)

winrm_wrapper.writeLog("Search end for " + uid)
print("__UID__=" + uid)
print("__NAME__=" + uid)
sys.exit()
