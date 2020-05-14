# Create script, which will create mailbox for existing user in remote Exchange
# INPUT:
#   uid - String - account identifier

Write-Host "PS Create started"
#Needed to load Exchange cmdlets
Add-PSSnapin -Name '*Exchange*'
try
{
    Enable-RemoteMailbox -Identity $uid

    #Create user with account in local AD
#    New-RemoteMailbox -Name "$name" `
#        -UserPrincipalName "$userPrincipalName" `
#        -SamAccountName "$uid" `
#        -Password (ConvertTo-SecureString -String "$password" -AsPlainText -Force) `
#        -OnPremisesOrganizationalUnit "$onPremisesOrganizationalUnit" `
#        -DisplayName "$displayName" `
#        -FirstName "$givenName" `
#        -LastName "$sn" `
#        -Confirm:$false


    Write-Host "Remote mailbox created"
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Create ended"