# Script, which will update mailbox for existing user in remote Exchange
# INPUT:
#   uid - String - account identifier

Write-Host "PS Update started"
#Needed to load Exchange cmdlets
Add-PSSnapin -Name '*Exchange*'
try
{
    Set-RemoteMailbox -Identity $uid #add more params to update
    Write-Host "Remote mailbox updated"
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Update ended"