# Delete script, which will remove o365 licence from user
# INPUT:
#   uid - String - account identifier

Write-Host "PS Delete started"
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
Write-Host "PS Delete ended"