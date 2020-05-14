# Create script, which will assign licence in o365 for user
# INPUT:
#   uid - String - account identifier

Write-Host "PS Create started"
try
{
    # Get AD user
    $User = Get-ADUser -Identity "$uid"
    if ($User -ne $Null)
    {
        $userPrincipalName = $User.UserPrincipalName
        # Get user from Azure
        $PWord = ConvertTo-SecureString -String "$adminPass" -AsPlainText -Force
        $Credential = New-Object System.Management.Automation.PSCredential ("$adminUser", $PWord)
        Write-Host "Credentials are prepared"
        Import-Module MSOnline
        Write-Host "Connecting to o365"
        Connect-MsolService -Credential $Credential
        Write-Host "Connected"
        $AzureUser = Get-MsolUser -UserPrincipalName "$userPrincipalName" -ErrorAction Stop
        if ($AzureUser -ne $Null)
        {
            if(-Not ($AzureUser.isLicensed)) {
                Write-Host "User exists in Azure a has no licence"
                # assign o365 licence
                Write-Host "We will assign o365 licence if user is new and without licence"
                Write-Host "Setting usage location"
                Set-MsolUser -UserPrincipalName "$userPrincipalName" -UsageLocation CZ -ErrorAction Stop
                Write-Host "Assigning licence"
                Set-MsolUserLicense -UserPrincipalName "$userPrincipalName" -AddLicenses "LINCENCE_NAME" -ErrorAction Stop
                Write-Host "Licence assigned"
            } else {
                Write-Host "User has licence already"
            }
        } else {
            throw "User does not exists in Azure"
        }
    } else {
        Write-Host "User $User does not exists"
    }
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Create ended"