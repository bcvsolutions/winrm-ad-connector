# winrm-ad-connector

This connector is based on our implementation of WinRM-connector and we added AD connector into it.
AD connector is bundled together and you can use AD and/or WinRM.

In folder scripts/NameOfSystem you can find python scripts for each supported operation method:
* Create
* Update
* Delete
* Test
* Search

Where NameOfSystem is one of these following values Exchange, OpenLims, o365, homeDir. If you want use this connector for  
system you can just implement scripts yourself. As a template you can use existing python + ps scripts.

PS scripts are in subfolders.

More details - https://wiki.czechidm.com/devel/documentation/systems/dev/winrm_ad_connector

For build you can use this command  
``$ mvn clean install -Dlicense.skip=true``  
