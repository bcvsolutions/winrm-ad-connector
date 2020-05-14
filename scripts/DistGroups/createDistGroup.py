#!/usr/bin/env python
#!python
# All params from IdM is stored in environment and you can get them by os.environ["paramName"]
import sys, os
# this is needed for importing file winrm_wrapper from parent dir
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import winrm_wrapper
import json
import codecs

uid = os.environ["__NAME__"]

winrm_wrapper.writeLog("Create start for " + uid)

# Parse multi valued attribute to list
#roles = json.loads(os.environ["roles"])

# Load PS script from file and replace params
winrm_wrapper.writeLog("loading script")
f = codecs.open(os.environ["script"], encoding='utf-8', mode='r')

command = f.read()
command = command.replace("$name", winrm_wrapper.getParam("name"))
command = command.replace("$mail", winrm_wrapper.getParam("mail"))
command = command.replace("$organizationalunit", winrm_wrapper.getParam("organizationalunit"))
command = command.replace("$displayName", winrm_wrapper.getParam("displayName"))

# Call wrapper
winrm_wrapper.executeScript(os.environ["endpoint"], os.environ["authentication"], os.environ["user"],
                                    os.environ["password"], command, uid)

winrm_wrapper.writeLog("Create end for " + uid)
print("__UID__=" + uid)
sys.exit()

