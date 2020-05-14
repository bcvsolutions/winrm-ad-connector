# Search script, which will serach home dir
# INPUT:
#   uid - String - account identifier

function returnJson
{
    Param ([String]$path, [String]$accountName)
    # convert output string into list which will contain Map
    $resultList = @()
    $resultMap = @{ }

    $name = "__UID__"
    $value = "$accountName"
    $resultMap.add("$name", "$value")
    $name = "__NAME__"
    $value = "$accountName"
    $resultMap.add("$name", "$value")
    $name = "path"
    $value = "$path"
    $resultMap.add("$name", "$value")

    $resultList += $resultMap

    # convert to json
    ConvertTo-Json $resultList
}

Write-Host "PS Search started"

#$fullPath = "\\NETWORK_DRIVE\DFS\Users1\UZIVATEL"
$fullPath = "\\NETWORK_DRIVE\DFS\Users1\$uid"

Write-Host "full path: $fullPath"

try
{
    if (Test-Path $fullPath -PathType Container)
    {
        Write-Host "HomeDir found"
        returnJson -path $fullPath -accountName $uid
    }
    else
    {
        Write-Host "HomeDir does not exists"
        returnJson -path "" -accountName ""
    }
}
catch
{
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Search ended"
