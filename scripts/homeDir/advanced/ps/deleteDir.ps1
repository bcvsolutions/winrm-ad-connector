# Delete script, which will delete home dir
# INPUT:
#   uid - String - account identifier

function DeletePermissions($path)
{
    $acl = get-acl $path
    $accessrule = New-Object system.security.AccessControl.FileSystemAccessRule("DOMAIN\$uid", "Read", ,,"Allow")
    $acl.RemoveAccessRuleAll($accessrule)
    Set-Acl -Path $path -AclObject $acl
    Write-Host "Permissions on Home dir for user $uid was rewoked"
}

Write-Host "PS Delete started"
#Disable loadin AD drive when I use AD command, The message was handled as error but is not error
$Env:ADPS_LoadDefaultDrive = 0

$newName = -join ("_", "$uid")

$hd1 = -join ("\\NETWORK_DRIVE\dfs\Users1\", "$newName")
$hd2 = -join ("\\NETWORK_DRIVE\dfs\Users2\", "$newName")
$hd3 = -join ("\\NETWORK_DRIVE\dfs\Users3\", "$newName")
$hd4 = -join ("\\NETWORK_DRIVE\dfs\Users4\", "$newName")
$hd5 = -join ("\\NETWORK_DRIVE\dfs\Users5\", "$newName")

Write-Host "my new folder name is: $newName"

try
{
    if (Test-Path $hd1 -PathType Container)
    {
        DeletePermissions $hd1
        Rename-Item -path $hd1 -NewName $newName -ErrorAction Stop
        Write-Host "Home dir for user $uid renamed"
    }
    ElseIf ( Test-Path $hd2 -PathType Container )
    {
        DeletePermissions $hd2
        Rename-Item -path $hd2 -NewName $newName -ErrorAction Stop
        Write-Host "Home dir for user $uid renamed"
    }
    ElseIf ( Test-Path $hd3 -PathType Container )
    {
        DeletePermissions $hd3
        Rename-Item -path $hd3 -NewName $newName -ErrorAction Stop
        Write-Host "Home dir for user $uid renamed"
    }
    ElseIf ( Test-Path $hd4 -PathType Container )
    {
        DeletePermissions $hd4
        Rename-Item -path $hd4 -NewName $newName -ErrorAction Stop
        Write-Host "Home dir for user $uid renamed"
    }
    ElseIf ( Test-Path $hd5 -PathType Container )
    {
        DeletePermissions $hd5
        Rename-Item -path $hd5 -NewName $newName -ErrorAction Stop
        Write-Host "Home dir for user $uid renamed"
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
