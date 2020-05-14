# Delete script, which will disable account in OpenLims, because deleting is not supported in IDMLIMS
# INPUT:
#   uid - String - account identifier

Write-Host "PS Delete started"
try
{
	# cd into folder where IDMLIMS is located
	cd D:\OpenLIMS\IDMLIMS_Test
	
    $logBefore = Get-Content Logs\err.txt
	
    $obj = .\IDMLIMS.exe disableUser $uid
    
	$logAfter = Get-Content \Logs\err.txt

    # Compare log before and after if some error was occured
    $result = Compare-Object $logBefore $logAfter
    if ($result.InputObject)
    {
        throw $result.InputObject
    }
    Write-Host "User with $uid disabled with result: $obj"
}
catch
{
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Delete ended"