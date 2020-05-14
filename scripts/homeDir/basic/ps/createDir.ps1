# Create script, which will create home dir with acl permissions
# INPUT:
#   uid - String - account identifier

Write-Host "PS Create started"
#Disable loadin AD drive when I use AD command, The message was handled as error but is not error
$Env:ADPS_LoadDefaultDrive = 0

$fullPath = "C:\test\home\$uid"

try
{
    $User = Get-ADUser -Identity $uid
    if($User -ne $Null)
    {
        if ( Test-Path $fullPath -PathType Container ) {
            throw "Directory already exists."
        }
        #homedir
        $homeShare = New-Item -path $fullPath -ItemType Directory -ea Stop
        Write-Host ("HomeDirectory created at {0}" -f $fullPath)

        #set rights to home folder
        $acl = Get-Acl $homeShare

        $FileSystemRights = [System.Security.AccessControl.FileSystemRights]"FullControl"
        $AccessControlType = [System.Security.AccessControl.AccessControlType]::Allow
        $InheritanceFlags = [System.Security.AccessControl.InheritanceFlags]"ContainerInherit, ObjectInherit"
        $PropagationFlags = [System.Security.AccessControl.PropagationFlags]"InheritOnly"

        $AccessRule = New-Object System.Security.AccessControl.FileSystemAccessRule ($User.SID, $FileSystemRights, $InheritanceFlags, $PropagationFlags, $AccessControlType)
        $acl.AddAccessRule($AccessRule)

        Set-Acl -Path $homeShare -AclObject $acl -ea Stop
        Write-Host ("ACL for HomeDirectory set")

        Set-ADUser $User -HomeDrive "C:" -HomeDirectory $fullPath -ea Stop
        Write-Host ("Home dir attr set to AD")
    }
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Create ended"