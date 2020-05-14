# Update script, which will update account in OpenLims
# INPUT:
#   uid - String - account identifier
#   firstName - String - First name
#   lastName - String - Last name
#   password - String - Password
#   titles - String - Titles before + titles after
#   middleName - String - Titles before + titles after
#   workName - String - lastName firstName titles
#   domain - String - AD domain
#   section - String - User's section
#   code - String - Code text - max 20 chars
#   email - String - Email
#   phone - String - Phone number
#   disabled - String - true or false if account is disabled or not
#   roles - String - list of roles in format - "-r role1 -r role2 -r roleN"

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

Write-Host "PS Update started"
try
{
	# cd into folder where IDMLIMS is located
	cd D:\OpenLIMS\IDMLIMS_Test
	
    Write-Host "Updating user $uid"

    $logBefore = Get-Content Logs\err.txt
    
	# We need to check if there is some value and if so then append it to the command
    $params
	
	$fn = "$firstName"
	if (-Not ([string]::IsNullOrEmpty($fn)))
    {
		$params += "-f $fn "
	}
	$ln = "$lastName"
	if (-Not ([string]::IsNullOrEmpty($ln)))
    {
		$params += "-l $ln "
	}
	$pass = "$password"
	if (-Not ([string]::IsNullOrEmpty($pass)))
    {
		$params += "-p $pass "
	}
	$t = "$titles"
	if (-Not ([string]::IsNullOrEmpty($t)))
    {
		$params += "-t $t "
	}
	$mn = "$middleName"
	if (-Not ([string]::IsNullOrEmpty($mn)))
    {
		$params += "-o $mn "
	}
	$wn = "$workname"
	if (-Not ([string]::IsNullOrEmpty($wn)))
    {
		$params += "-j $wn "
	}
	$d = "$domain"
	if (-Not ([string]::IsNullOrEmpty($d)))
    {
		#$params += "-adsync given -d $d "
	}
	$s = "$section"
	if (-Not ([string]::IsNullOrEmpty($s)))
    {
		$params += "-u $s "
	}
	$c = "$code"
	if (-Not ([string]::IsNullOrEmpty($c)))
    {
		$params += "-c $c "
	}
	$e = "$email"
	if (-Not ([string]::IsNullOrEmpty($e)))
    {
		$params += "-m $e "
	}
	$p = "$phone"
	if (-Not ([string]::IsNullOrEmpty($p)))
    {
		$params += "-e $p "
	}
	$obj = iex ".\IDMLIMS.exe updateUser $uid $params"
    
	$logAfter = Get-Content Logs\err.txt

    checkForErrors -a $logBefore -b $logAfter
    Write-Host "User with $uid updated with result: $obj"

	if ($obj -ne 0) {
		throw $obj
	}

    # If user should be updated as disabled we need to call other method in IDMLIMS
    if ("$disabled" -eq "true")
    {
        Write-Host "User $uid will be disabled"

        $obj = .\IDMLIMS.exe disableUser $uid
        
		$logAfterDisable = Get-Content Logs\err.txt
        checkForErrors -a $logAfter -b $logAfterDisable

        Write-Host "User $uid is disabled with result: $obj"
    }

    # If user should be updated with some roles we need to call another update
    if (-Not ([string]::IsNullOrEmpty("$roles")))
    {
        Write-Host "Assign $roles to user $uid"

        # Actual syntax for roles is -r role1 -r role2 -r roleN
        # But we are replacing this variables from python so we can't send list into this script.
        # Role parameter must be in format "-r role1 -r role2 -r roleN"
        $obj = .\IDMLIMS.exe updateUser $uid $roles
        
		$logAfterUpdate = Get-Content Logs\err.txt
        checkForErrors -a $logAfter -b $logAfterUpdate

        Write-Host "Roles assigned to $uid with result: $obj"
    }
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Update ended"