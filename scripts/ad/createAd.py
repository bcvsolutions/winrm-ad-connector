#!/usr/bin/env python
# -*- coding: utf-8 -*-
# All params from IdM is stored in environment and you can get them by os.environ["paramName"]
import sys, os
# this is needed for importing file winrm_wrapper from parent dir
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import winrm_wrapper
import json
import codecs

uid = winrm_wrapper.getParam("sAMAccountName")
dn = winrm_wrapper.getParam("__NAME__")

winrm_wrapper.writeLog("Create start for " + uid)

# Parse multi valued attribute to list
if winrm_wrapper.getParam("ldapGroupsToAdd"):
    rolesToAddArgument = winrm_wrapper.getMultivalue("ldapGroupsToAdd")
else:
    rolesToAddArgument = ""

if winrm_wrapper.getParam("ldapGroupsToRemove"):
    rolesToRemoveArgument = winrm_wrapper.getMultivalue("ldapGroupsToRemove")
else:
    rolesToRemoveArgument = ""

if not rolesToAddArgument and not rolesToRemoveArgument:
    winrm_wrapper.writeLog("Create end for " + uid + " no roles to remove or assign")
    print("__UID__=" + uid)
    sys.exit()

winrm_wrapper.writeLog("roles argumet to add " + rolesToAddArgument)
winrm_wrapper.writeLog("roles argumet to remove " + rolesToRemoveArgument)

if winrm_wrapper.getParam("additionalCreds"):
    credentialsArgument = winrm_wrapper.getMultivalue("additionalCreds")
else:
    credentialsArgument = ""

# Load PS script from file and replace params
winrm_wrapper.writeLog("loading script")
f = codecs.open(os.environ["script"], encoding='utf-8', mode='r')
command = f.read()
command = command.replace("$uid", uid)
command = command.replace("$ldapGroupsToAdd", rolesToAddArgument)
command = command.replace("$ldapGroupsToRemove", rolesToRemoveArgument)
command = command.replace("$additionalCreds", credentialsArgument)
command = command.replace("$dn", dn)

# Call wrapper
winrm_wrapper.executeScript(os.environ["endpoint"], os.environ["authentication"], os.environ["user"],
                                    os.environ["password"], os.environ["caTrustPath"], os.environ["ignoreCaValidation"], command, uid)

winrm_wrapper.writeLog("Create end for " + uid)
print("__UID__=" + uid)
sys.exit()
