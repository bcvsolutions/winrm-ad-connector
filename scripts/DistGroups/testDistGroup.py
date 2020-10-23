#!/usr/bin/env python
#!python
# All params from IdM is stored in environment and you can get them by os.environ["paramName"]
import sys, os
# this is needed for importing file winrm_wrapper from parent dir
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
import winrm_wrapper

winrm_wrapper.writeLog("Test start")

# Call wrapper
winrm_wrapper.executeScript(os.environ["endpoint"], os.environ["authentication"], os.environ["user"],
                                    os.environ["password"], os.environ["caTrustPath"], os.environ["ignoreCaValidation"])

winrm_wrapper.writeLog("Test end")
sys.exit()


