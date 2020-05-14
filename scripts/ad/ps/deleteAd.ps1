# Delete script, which will delete ad user
# INPUT:
#   uid - String - account identifier

Write-Host "PS Delete started"
#Disable loadin AD drive when I use AD command, The message was handled as error but is not error
$Env:ADPS_LoadDefaultDrive = 0

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
Write-Host "PS Delete ended"