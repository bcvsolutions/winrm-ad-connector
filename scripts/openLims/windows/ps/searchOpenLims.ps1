# Search script, which will search account in OpenLims
# INPUT:
#   uid - String - account identifier

function checkForErrors
{
    Param ([String]$a, [String]$b)
    # Compare log before and after if some error was occured
    $result = Compare-Object $a $b
    if ($result.InputObject)
    {
        throw $result.InputObject
    }
}

$openLimsPath = "C:\OpenLims"

Write-Output "PS Search started"
try
{
	# cd into folder where IDMLIMS is located
	cd $openLimsPath
	
	$logBefore = Get-Content Logs\err.txt
    
	$identificator = "$uid"
	if ([string]::IsNullOrEmpty($identificator))
    {
		# uid is empty we are searching for all users
		$obj = .\IDMLIMS.exe listUsers
    }
	else
    {
	# searching for one user
	$obj = .\IDMLIMS.exe getUser $identificator
	}
	$logAfter = Get-Content Logs\err.txt
	
	# check for errors in log files
	checkForErrors -a $logBefore -b $logAfter
	
	# convert output string into list which will contain Map
	$resultList = @()
	$csv = $obj | ConvertFrom-Csv -Delim ';'
	foreach($item in $csv) {
		$resultMap = @{}
		foreach($attr in $item.psobject.Properties)
		{
			$name = $attr.Name
			# We need to change name of few attribute so it will be same as in IdM
			if ($name -eq "firstname")
			{
				$name = "firstName"
			}
			if ($name -eq "lastname")
			{
				$name = "lastName"
			}
			if ($name -eq "titul")
			{
				$name = "titles"
			}
			if ($name -eq "pracjmeno")
			{
				$name = "workName"
			}
			$value = $attr.Value
			$resultMap.add("$name", "$value")
			# we need to fill __NAME__ and __UID__ attributes manually
			if ($name -eq "login")
			{
				$name = "__UID__"
				$resultMap.add("$name", "$value")
				$name = "__NAME__"
				$resultMap.add("$name", "$value")
			}
		}
		$resultList += $resultMap
	}
		
	# convert to json
	ConvertTo-Json $resultList
}
catch
{
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Output "PS Search ended"
