# Delete script, which will delete home dir
# INPUT:
#   uid - String - account identifier

Write-Host "PS Delete started"
#Disable loadin AD drive when I use AD command, The message was handled as error but is not error
$Env:ADPS_LoadDefaultDrive = 0

$fullPath = "C:\test\home\$uid"

try
{
	if ( Test-Path $fullPath -PathType Container ) {
		Remove-Item -path $fullPath -recurse
		Write-Host "Home dir for user $uid deleted"
    }
    else
    {
        Write-Host "HomeDir does not exists"
    }
}
catch
{
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Delete ended"