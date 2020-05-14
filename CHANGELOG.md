# CHANGELOG

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



