#!/usr/bin/env python
# -*- coding: utf-8 -*-
# All params from IdM is stored in environment and you can get them by os.environ["paramName"] or winrm_wrapper.getParam("paramName"]
import sys, os
# this is needed for importing file winrm_wrapper from parent dir
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import winrm_wrapper
import json
import codecs

# __UID__ cant be in create attributes, because of connector-framework
uid = winrm_wrapper.getParam("__NAME__")

winrm_wrapper.writeLog("Create start for " + uid)

# Parse multi valued attribute to list
if winrm_wrapper.getParam("roles"):
    roles = json.loads(winrm_wrapper.getParam("roles"))
    rolesArgument = ""
    for role in roles:
        rolesArgument += "-r " + role
else:
    rolesArgument = ""
    
# Load PS script from file and replace params
winrm_wrapper.writeLog("loading script")
f = codecs.open(os.environ["script"], encoding='utf-8', mode='r')
command = f.read()
command = command.replace("$uid", uid)
command = command.replace("$firstName", winrm_wrapper.getParam("firstName"))
command = command.replace("$lastName", winrm_wrapper.getParam("lastName"))
command = command.replace("$password", winrm_wrapper.getParam("__PASSWORD__"))
command = command.replace("$titles", winrm_wrapper.getParam("titles"))
command = command.replace("$middleName", winrm_wrapper.getParam("middleName"))
command = command.replace("$workName", winrm_wrapper.getParam("workName"))
command = command.replace("$domain", winrm_wrapper.getParam("domain"))
command = command.replace("$section", winrm_wrapper.getParam("section"))
command = command.replace("$code", winrm_wrapper.getParam("code"))
command = command.replace("$email", winrm_wrapper.getParam("email"))
command = command.replace("$phone", winrm_wrapper.getParam("phone"))
command = command.replace("$disabled", winrm_wrapper.getParam("disabled"))
command = command.replace("$roles", rolesArgument)

# Call wrapper
winrm_wrapper.executeScript(os.environ["endpoint"], os.environ["authentication"], os.environ["user"],
                            os.environ["password"], command, uid)

winrm_wrapper.writeLog("Create end for " + uid)
print("__UID__=" + uid)
sys.exit()
