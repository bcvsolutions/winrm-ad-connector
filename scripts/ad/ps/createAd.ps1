# Create script, which will update AD groups for user
# INPUT:
#   uid - String - account identifier
#   ldapGroupsToAdd - Strig - String which contain comma separated string values so we can iterate thru it later in this script
#   ldapGroupsToRemove - Strig - String which contain comma separated string values so we can iterate thru it later in this script

$adminUser = $null
$adminPass = $null

function getCredentials
{
    Param ([String]$userDn, [hashtable]$map)

    [hashtable]$result = @{}
    foreach($key in $map.keys)
    {
        if ($userDn -like "*$key")
        {
            $adminUser, $adminPass = $map[$key] -split '\\',2
            $result.adminUser = $adminUser
            $result.adminPass = $adminPass
            $adminUser = $null
            $adminPass = $null
        }
    }
    return $result
}

Write-Host "PS Craete started"
#Disable loadin AD drive when I use AD command, The message was handled as error but is not error
$Env:ADPS_LoadDefaultDrive = 0

$rolesToAdd = @($ldapGroupsToAdd)
$rolesToRemove = @($ldapGroupsToRemove)
Write-Host "Roles to add $rolesToAdd"
Write-Host "Roles to remove $rolesToRemove"

$credentialsMap = @{}
$credentialsList = @($additionalCreds)
foreach ($cred in $credentialsList) {
    $server, $credentialPart = $cred -split '\\',2
    $credentialsMap.add($server, $credentialPart)
    $server = $null
    $credentialPart = $null
}

try
{
    $creds = getCredentials -userDn "$dn" -map $credentialsMap

    $userDomain = "$dn".Substring("$dn".IndexOf("DC=")).Replace("DC=","").Replace(",",".")

    Write-Host "domain $userDomain"

    $PWord = ConvertTo-SecureString -String "$($creds.adminPass)" -AsPlainText -Force
    $Credential = New-Object System.Management.Automation.PSCredential ("$userDomain\$($creds.adminUser)", $PWord)

    $User = Get-ADUser -Identity $uid -Server $userDomain -Credential $Credential

    foreach ($roleToAdd in $rolesToAdd) {
        Write-Host "Assign $roleToAdd to user $uid"
        # get domain name from DN of group
        $domain = $roleToAdd.Substring($roleToAdd.IndexOf("DC=")).Replace("DC=","").Replace(",",".")

        $creds = getCredentials -userDn $roleToAdd -map $credentialsMap

        $PWord = ConvertTo-SecureString -String "$($creds.adminPass)" -AsPlainText -Force
        $Credential = New-Object System.Management.Automation.PSCredential ("$domain\$($creds.adminUser)", $PWord)

        Add-ADGroupMember -Identity $roleToAdd -Members $User -Server $domain -Credential $Credential -Confirm:$false -ErrorAction Stop
        Write-Host "Role assigned to user $uid"
    }
    foreach ($roleToRemove in $rolesToRemove) {
        Write-Host "Remove $roleToRemove from user $uid"
        # get domain name from DN of group
        $domain = $roleToRemove.Substring($roleToRemove.IndexOf("DC=")).Replace("DC=","").Replace(",",".")

        $creds = getCredentials -userDn $roleToRemove -map $credentialsMap

        $PWord = ConvertTo-SecureString -String "$($creds.adminPass)" -AsPlainText -Force
        $Credential = New-Object System.Management.Automation.PSCredential ("$domain\$($creds.adminUser)", $PWord)

        Remove-ADGroupMember -Identity $roleToRemove -Members $User -Server $domain -Credential $Credential -Confirm:$false -ErrorAction Stop
        Write-Host "Role removed from user $uid"
    }
    Write-Host "Roles updated"

    $credentialsMap = $null
    $credentialsList = $null
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
finally
{
    $credentialsMap = $null
    $credentialsList = $null
    $creds = $null
}
Write-Host "PS Create ended"