# Update script, which will update exchange account for existing user in AD
# INPUT:
#   uid - String - account identifier

Write-Host "PS Update started"
#Needed to load Exchange cmdlets
Add-PSSnapin -Name '*Exchange*'
try
{
    Set-Mailbox -Identity $uid #add more params to update
    Write-Host "Mailbox updated"
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Update ended"