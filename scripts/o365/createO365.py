#!/usr/bin/env python
# -*- coding: utf-8 -*-
# All params from IdM is stored in environment and you can get them by os.environ["paramName"]
import sys, os
# this is needed for importing file winrm_wrapper from parent dir
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import winrm_wrapper
import codecs

# __UID__ cant be in create attributes, because of connector-framework
uid = os.environ["sAMAccountName"]

winrm_wrapper.writeLog("Create start for " + uid)

# Load PS script from file and replace params
winrm_wrapper.writeLog("loading script")
f = codecs.open(os.environ["script"], encoding='utf-8', mode='r')
command = f.read()
command = command.replace("$uid", uid)
command = command.replace("$adminPass", os.environ["password"])
command = command.replace("$adminUser", os.environ["user"])

# Call wrapper
winrm_wrapper.executeScript(os.environ["endpoint"], os.environ["authentication"], os.environ["user"],
                                    os.environ["password"], command, uid)

winrm_wrapper.writeLog("Create end for " + uid)
print("__UID__=" + uid)
sys.exit()
