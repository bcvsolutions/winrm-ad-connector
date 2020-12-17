# Create script, which will craete account in OpenLims
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

$openLimsPath = "C:\OpenLims"

#Write-Host "PS Create started"
try
{
	# cd into folder where IDMLIMS is located
	cd $openLimsPath

    Write-Output "Creating user $uid"

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
		$params += "-t '$t' "
	}
	$mn = "$middleName"
	if (-Not ([string]::IsNullOrEmpty($mn)))
    {
		$params += "-o $mn "
	}
	$wn = "$workName"
	if (-Not ([string]::IsNullOrEmpty($wn)))
    {
		$params += "-j '$wn' "
	}
	$d = "$domain"
	if (-Not ([string]::IsNullOrEmpty($d)))
    {
		$params += "-adsync given -d $d "
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
	$obj = iex ".\IDMLIMS.exe createUser $uid $params"
    
	
	$logAfter = Get-Content Logs\err.txt
    checkForErrors -a $logBefore -b $logAfter
	
	if ($obj -ne 0) {
		throw $obj
	}

    # If user should be created as disabled we need to call other method in IDMLIMS
    if ("$disabled" -eq "true")
    {
        Write-Output "User $uid will be disabled"

        $obj = .\IDMLIMS.exe disableUser $uid

	$logAfterDisable = Get-Content Logs\err.txt
        checkForErrors -a $logAfter -b $logAfterDisable

        Write-Output "User $uid is disabled with result: $obj"
    }

    # If user should have some roles we need to call update
    if (-Not ([string]::IsNullOrEmpty("$roles")))
    {
        Write-Output "Assign $roles to user $uid"

        # Actual syntax for roles is -r role1 -r role2 -r roleN
        # But we are replacing this variables from python so we can't send list into this script.
        # Role parameter must be in format "-r role1 -r role2 -r roleN"
        
        $obj = iex ".\IDMLIMS.exe updateUser $uid $roles"
        
	$logAfterUpdate = Get-Content Logs\err.txt
        checkForErrors -a $logAfter -b $logAfterUpdate

        Write-Output "Roles assigned to $uid with result: $obj"
    }
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Output "PS Create ended"
