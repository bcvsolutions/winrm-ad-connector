# -*- coding: utf-8 -*-
import sys, os
import winrm
import datetime
import logging
import json
import re
from base64 import b64encode


# logging.basicConfig(level=logging.DEBUG)

# Get param from environment and return it as unicode
def getParam(name):
    try:
        return unicode(os.environ[name], 'utf-8')
    except:
        return ""


# Parse multivalue attribute
def getMultivalue(name):
    if name != "additionalCreds":
        writeLog("Value of param " + name + ": " + getParam(name))

    values = json.loads(getParam(name))
    parsedValues = ""
    for value in values:
        parsedValues += "\"" + value + "\","

    return parsedValues.rstrip(",")


# Write message with current time to stdout
def writeLog(message):
    currentDT = datetime.datetime.now()
    print(currentDT.strftime("%Y-%m-%d %H:%M:%S") + " " + str(message))

    # use this for debug. Create .txt file in the path or change the path. Then you will see "real time" log from python
    # f = open("/opt/connector-server/log.txt", "a")
    # f.write(currentDT.strftime("%Y-%m-%d %H:%M:%S") + " " + str(message))
    # f.close()


def encode(script):
    writeLog("Encoding started")

    # Copyright (C) 2012  Carlos Perez
    # https://github.com/darkoperator/powershell_scripts/blob/master/ps_encoder.py

    # blank command will store our fixed unicode variable
    blank_command = ""
    encoded_cmd = ""
    # Remove weird chars that could have been added by ISE
    n = re.compile(u'(\xef|\xbb|\xbf)')
    # loop through each character
    for char in (n.sub("", script)):
        blank_command += char
    # assign powershell command as the new one
    encoded_cmd = blank_command
    # base64 encode the powershell command
    encoded_cmd = b64encode(encoded_cmd.encode('utf_16_le'))

    # writeLog("Encoding finnished: " + encoded_cmd)
    return encoded_cmd


# It execute script via WinRM, if no script is send via params it will use default Write-Host for connection test
def executeScript(endpoint, authentication, user, password, ca_trust_path, ignore_validation=False, script=None, uid=None):
    writeLog("Winrm wrapper start")

    # Validate params which are mandatory
    if not endpoint:
        writeLog("Endpoint is required - Winrm wrapper end")
        sys.exit(1)
    if not authentication:
        writeLog("Authentication is required - Winrm wrapper end")
        sys.exit(1)
    if not user:
        writeLog("User is required - Winrm wrapper end")
        sys.exit(1)
    if not password:
        writeLog("Password is required - Winrm wrapper end")
        sys.exit(1)
    if not script:
        writeLog("No script is passed, it use only default Write-Host command for testing purpose")
        script = "Write-Host connection test OK"
    if not uid:
        writeLog("Uid not set")
        # uid = "empty"

    try:
        writeLog("Calling WinRM and running script")

        # Powershell command which will load our powershell from env and execute it.
        cmd = """
        $source = [Text.Encoding]::Unicode.GetString([Convert]::FromBase64String($Env:WINRM_SCRIPT))
        . ([ScriptBlock]::Create($source))
        """

        # Encode our powershell command
        encodedScript = encode(script)
        # Encode cmd powershell
        encodedCmd = encode(cmd)

        if ignore_validation == True:
            p = winrm.protocol.Protocol(
                endpoint=endpoint,
                transport=authentication,
                username=user,
                password=password,
                server_cert_validation='ignore')
        else:
            # Prepare session to WinRM and run script
            # Need to specify ca_trust_path - its path to the .pem certificate
            p = winrm.protocol.Protocol(
                endpoint=endpoint,
                transport=authentication,
                username=user,
                password=password,
                ca_trust_path=ca_trust_path)

        # Set out command to env variable beacause cmd has some limit for executing command send via string. This is workaround for executing larger scripts
        shell = p.open_shell(env_vars=dict(WINRM_SCRIPT=encodedScript))
        # Run command via WinRM
        command = p.run_command(shell, "powershell -EncodedCommand {}".format(encodedCmd))

        # Get reposnse
        r = winrm.Response(p.get_command_output(shell, command))

        p.cleanup_command(shell, command)
        p.close_shell(shell)

        writeLog("Output:\n" + r.std_out)
        writeLog("Status code:" + str(r.status_code))

        # Check if PS script ended with code 0
        # PS script can exit with 0 and still output some error if the exception is not handled,
        # so we check if there are some messages in std_err
        if r.status_code > 0 or not len(r.std_err) == 0:
            # If return code is 0 but we found CLIXML in std.err just log it and ignore it, because it's not an error
            if "#< CLIXML" in r.std_err and r.status_code == 0:
                writeLog("PS execution returned CLIMXML it's not really error so we only log it")
                writeLog("Warning:" + r.std_err)
                r.std_err = ""
            else:
                writeLog("PS execution failed - Winrm wrapper end")
                sys.stderr.write(r.std_err)
                # We need to return this attributes back, otherwise the connector will throw error
                print("__UID__=" + str(uid))
                print("__NAME__=" + str(uid))
                sys.exit(1)
    except Exception as inst:
        writeLog("Error when connecting to WinRM - Winrm wrapper end")
        sys.stderr.write(str(type(inst)))
        sys.stderr.write(str(inst.args))

        # We need to return this attributes back, otherwise the connector will throw error
        print("__UID__=" + str(uid))
        print("__NAME__=" + str(uid))
        sys.exit(1)

    writeLog("Winrm wrapper end")
