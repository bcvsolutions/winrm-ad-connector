# CHANGELOG

##### 23.10.2020 - [1.0.5]
* new versions of com.fasterxml.jackson.core libs. jackson-databind had some vulnerability in version 2.9.8. We used this opportunity
to update all jackson dependencies to latest versions
* Path to certificate which is used for SSL WinRM communication can be set via connector configuration together with the option to skip certification validation
You need to update your python scripts and winrm_wrapper.py if you want to use this new configuration options. If you leave your scripts without changes it will work as in previous versions.
* SEVERE log message is print to log only if some error occurs. Previously it was logged every time even with empty body.
* Connector accepts OperationalOptions and filter attributes if some of them is only for WinRM part.
* You don't need to fill path to all script. Now filled path is only checked for the operations which you selected.
* If you search by AD and WinRM together, \_\_NAME\_\_ attribute is used from AD. 

##### 15.04.2020 - [1.0.4]
* New feature of configuration options for WinRM service, path to powershell scripts, which type of connection will be used and 
in which order. Added option to use connector in multi-domain environment.
* Hidden logging of each variable which will be set to scripts, because of potential of leaking sensitive information.
* New feature of executing CRUD operations via AD connector or by executing script, which means we can execute powershell on
target system. Each operation can be executed by none, one or by both methods.
* Fixed issue where \_\_NAME\_\_ was returned instead of uid in create operation.
* New feature of setting attributes into script even for delete and test operation.
* New feature waitFor method is returning uid from scripts.
* New feature of exit codes handling.
* New feature of sending multivalued attribute as json instead of comma separated String. Previous solutions caused issue
if some value in this multivalued attribute contained comma.
* New feature for attributes with roles. In AD we use ldapGroups, ldapGroupsToRemove, ldapGroupsToAdd and general roles.
In case attribute is one from above mentioned we will always send it as multivalued.
* New feature for handling response from scripts. We are parsing the response from json into Java object. Powershell script for searching is
returning data in json format. This solution provide us better solution how to return complex objects from scripts.



