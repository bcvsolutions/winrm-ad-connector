# Create script, which will create home dir with acl permissions
# INPUT:
#   uid - String - account identifier
# *** FUNCTION DEFINITIONS

function WriteItemToFile($content)
{
    if (!(Test-Path \\SERVER\homeDirLogs\reusingHomeDir.txt))
    {
        New-Item -path "\\NETWORK_DRIVE\homeDirLogs" -name reusingHomeDir.txt -type "file"
        Write-Host "Created new file"
    }
    Add-Content -path \\NETWORK_DRIVE\homeDirLogs\reusingHomeDir.txt -value $content
    Write-Host "New text content added"
}

function MoveFile($path)
{
    Move-Item -Path $path -Destination $notUsedHomeDir
}

# *** BEGIN MAIN SCRIPT
Write-Host "PS Create started"
#Disable loadin AD drive when I use AD command, The message was handled as error but is not error
$Env:ADPS_LoadDefaultDrive = 0

Write-Host "my attribute company: $homeDirectory"
Write-Host "my edited attribute $notUsedHomeDir"

try
{
    $newName = -join ("_", "$uid")

    $hd1 = -join ("\\NETWORK_DRIVE\dfs\Users1\", "$newName")
    $hd2 = -join ("\\NETWORK_DRIVE\dfs\Users2\", "$newName")
    $hd3 = -join ("\\NETWORK_DRIVE\dfs\Users3\", "$newName")
    $hd4 = -join ("\\NETWORK_DRIVE\dfs\Users4\", "$newName")
    $hd5 = -join ("\\NETWORK_DRIVE\dfs\Users5\", "$newName")

    if (-Not( $hd1 -eq "$notUsedHomeDir") -And (Test-Path $hd1 -PathType Container))
    {
        MoveFile $hd1
    }
    ElseIf (-Not( $hd2 -eq "$notUsedHomeDir") -And (Test-Path $hd2 -PathType Container))
    {
        MoveFile $hd2
    }
    ElseIf (-Not( $hd3 -eq "$notUsedHomeDir") -And (Test-Path $hd3 -PathType Container))
    {
        MoveFile $hd3
    }
    ElseIf (-Not( $hd4 -eq "$notUsedHomeDir") -And (Test-Path $hd4 -PathType Container))
    {
        MoveFile $hd4
    }
    ElseIf (-Not( $hd5 -eq "$notUsedHomeDir") -And (Test-Path $hd5 -PathType Container))
    {
        MoveFile $hd5
    }

    if (Test-Path $notUsedHomeDir -PathType Container)
    {
        Rename-Item -Path $notUsedHomeDir -NewName $uid
        $line = -join ((Get-Date).ToString('dd/MM/yyyy HH:mm:ss'), ";$homeDirectory;reused directory")
        WriteItemToFile $line
    }
    else
    {
        NEW-ITEM –path $homeDirectory -type directory -force
    }

    $rule_parameters = @(
    "DOMAIN\$uid"
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

    $acl | Set-Acl $homeDirectory
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Create ended"
