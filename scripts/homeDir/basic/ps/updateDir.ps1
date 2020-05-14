# Update script, which will update home dir
# INPUT:
#   uid - String - account identifier

Write-Host "PS Update started"
try
{
    #throw "Update is not supported."
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Update ended"