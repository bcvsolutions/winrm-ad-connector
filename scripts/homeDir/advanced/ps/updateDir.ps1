# Create script, which will create home dir with acl permissions
# INPUT:
#   uid - String - account identifier

# *** FUNCTION DEFINITIONS

function WriteItemToFile($content)
{

    if (!(Test-Path \\SERVER\homeDirLogs\reusingHomeDir.txt))
    {
        New-Item -path "\\SERVER\homeDirLogs" -name reusingHomeDir.txt -type "file"
        Write-Host "Created new file"
    }
    Add-Content -path \\SERVER\homeDirLogs\reusingHomeDir.txt -value $content
    Write-Host "New text content added"
}

function MoveFile($path, $newDest)
{
    Move-Item -Path $path -Destination $newDest
    Write-Host "HomeDir moved"
}

function CheckAndMoveFile($newName, $newDest)
{

    $hd1 = -join ("\\NETWORK_DRIVE\dfs\Users1\", "$newName")
    $hd2 = -join ("\\NETWORK_DRIVE\dfs\Users2\", "$newName")
    $hd3 = -join ("\\NETWORK_DRIVE\dfs\Users3\", "$newName")
    $hd4 = -join ("\\NETWORK_DRIVE\dfs\Users4\", "$newName")
    $hd5 = -join ("\\NETWORK_DRIVE\dfs\Users5\", "$newName")

    if (-Not( $hd1 -eq $newDest) -And (Test-Path $hd1 -PathType Container))
    {
        MoveFile $hd1 $newDest
        AddPermissionOnFolder $newDest
    }
    ElseIf (-Not( $hd2 -eq $newDest) -And (Test-Path $hd2 -PathType Container))
    {
        MoveFile $hd2 $newDest
        AddPermissionOnFolder $newDest
    }
    ElseIf (-Not( $hd3 -eq $newDest) -And (Test-Path $hd3 -PathType Container))
    {
        MoveFile $hd3 $newDest
        AddPermissionOnFolder $newDest
    }
    ElseIf (-Not( $hd4 -eq $newDest) -And (Test-Path $hd4 -PathType Container))
    {
        MoveFile $hd4 $newDest
        AddPermissionOnFolder $newDest
    }
    ElseIf (-Not( $hd5 -eq $newDest) -And (Test-Path $hd5 -PathType Container))
    {
        MoveFile $hd5 $newDest
        AddPermissionOnFolder $newDest
    }
}

function AddPermissionOnFolder($HomeDirPath)
{
    $rule_parameters = @(
    "DOMAIN\$samaccountName"
    "FullControl"
    ,@(
    "ContainerInherit"
    "ObjectInherit"
    )
    "None"
    "Allow"
    )

    $acl = (Get-Item $homeDirectory).GetAccessControl('Access')

    $rule = New-Object `
		-TypeName System.Security.AccessControl.FileSystemAccessRule `
		-ArgumentList $rule_parameters

    $acl.SetAccessRule($rule)

    $acl | Set-Acl $HomeDirPath
}

# *** BEGIN MAIN SCRIPT

Write-Host "PS Update started"
#Disable loadin AD drive when I use AD command, The message was handled as error but is not error
$Env:ADPS_LoadDefaultDrive = 0

Write-Host "update - uid: $uid"

try
{
    if (-Not("$samaccountName" -eq "$uid"))
    {
        CheckAndMoveFile $uid $homeDirectory
        if (Test-Path $homeDirectory -PathType Container)
        {
            Write-Host "Home dir for user $samaccountName renamed"
            $line = -join ((Get-Date).ToString('dd/MM/yyyy HH:mm:ss'), ";$homeDirectory;renamed directory")
            WriteItemToFile $line
        }
    }
    if (-Not(Test-Path $homeDirectory -PathType Container))
    {

        $newName = -join ("_", "$uid")
        CheckAndMoveFile $newName $notUsedHomeDir

        CheckAndMoveFile $uid $homeDirectory
        if (-Not(Test-Path $homeDirectory -PathType Container))
        {

            if (Test-Path $notUsedHomeDir -PathType Container)
            {
                Rename-Item -Path $notUsedHomeDir -NewName $uid
                $line = -join ((Get-Date).ToString('dd/MM/yyyy HH:mm:ss'), ";$homeDirectory;reused directory")
                WriteItemToFile $line
                Write-Host "HomeDir renamed"
            }
            else
            {
                NEW-ITEM –path $homeDirectory -type directory -force
                Write-Host "Created new HomeDir"
            }

            AddPermissionOnFolder $homeDirectory

            Write-Host "Home directory $homeDirectory created"
        }
    }
    else
    {
        Write-Host "Home directory $homeDirectory was already existing"
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
