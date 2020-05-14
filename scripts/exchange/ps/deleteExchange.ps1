# Delete script, which will delete exchange account and user in AD will still remain
# INPUT:
#   uid - String - account identifier

Write-Host "PS Delete started"
#Needed to load Exchange cmdlets
Add-PSSnapin -Name '*Exchange*'
try
{
    Disable-Mailbox -Identity $uid
    Write-Host "Mailbox deleted"
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Delete ended"