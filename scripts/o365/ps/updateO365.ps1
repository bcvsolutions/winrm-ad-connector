# Script, which will update licence for user in o365
# INPUT:
#   uid - String - account identifier

Write-Host "PS Update started"
#Needed to load Exchange cmdlets
try
{
    Write-Host "Not supported now"
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Update ended"