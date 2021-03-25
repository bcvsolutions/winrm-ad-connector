# Exchange scripts

Example of scripts that support CRUD operations on Exchange with minimal set of attributes.

Schema and mapping in IdM should have following attributes:

* sAMAccountName - identifier (update is not supported)
* Database - name of the Exchange database for mailbox creation (update is not supported)
* PrimarySmtpAddress - primary mail address of the user
