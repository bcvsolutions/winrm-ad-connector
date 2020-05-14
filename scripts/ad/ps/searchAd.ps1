# Search script, which will serach ad user
# INPUT:
#   uid - String - account identifier

Write-Host "PS Search started"

try
{
        Write-Host "Nothing"
}
catch
{
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Search ended"
