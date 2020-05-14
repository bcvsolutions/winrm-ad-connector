# Search script, which will return information about users licence from o365
# INPUT:
#   uid - String - account identifier

Write-Host "PS Search started"
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
Write-Host "PS Search ended"