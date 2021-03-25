# winrm-ad-connector

This connector is based on our implementation of WinRM-connector and we added AD connector into it.
AD connector is bundled together and you can use AD and/or WinRM.

In folder scripts/NameOfSystem you can find python scripts for each supported operation method:
* Create
* Update
* Delete
* Test
* Search

Where NameOfSystem is one of these following values Exchange, OpenLims, o365, homeDir. If you want use this connector for other systems, you can just implement scripts yourself. As a template you can use existing python + ps scripts.

PS scripts are in subfolders:
* scripts - These scripts can be used for WinRM+AD connector running on the system with default Python 2 (e.g. CentOS 7).
* scripts_python3 - These scripts can be used for WinRM+AD connector running on the system with default Python 3 (e.g. CentOS 8) or when using the appliance BCV Greeno - identity platform. These scripts require minimal version of WinRM+AD connector to be 1.0.6 and support the configuration options introduced in 1.0.5.

More details - https://wiki.czechidm.com/devel/documentation/adm/systems/winrm_ad_connector

For build you can use this command  
``$ mvn clean install -Dlicense.skip=true``  
