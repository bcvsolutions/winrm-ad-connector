# Search script, which will return information about user's exchange account
# INPUT:
#   uid - String - account identifier

Write-Host "PS Search started"
#Needed to load Exchange cmdlets
Add-PSSnapin -Name '*Exchange*'
try
{
    $identificator = "$uid"
    $obj
    # parsing properties for IdM
    $resultList = @()

    if ( [string]::IsNullOrEmpty($identificator))
    {
        $obj = Get-Mailbox
    }
    else
    {
        $User = Get-ADUser -Identity $identificator
        if ($Null -eq $User) {
            return ConvertTo-Json $resultList
        }
        $obj = Get-Mailbox -Identity $identificator
    }

    if ($Null -eq $obj) {
        $resultMap = @{ }
        $resultList += $resultMap
    } else {
        foreach ($item in $obj)
        {
            $resultMap = @{ }
            foreach ($attr in $item.psobject.Properties)
            {
                if (![string]::IsNullOrWhitespace($attr.Value))
                {
                    $name = $attr.Name
                    $value = $attr.Value
		            $value = $value -replace "\\", "\\"
                    $resultMap.add("$name", "$value")
                    # we need to fill __NAME__ and __UID__ attributes manually
                    if ($name -eq "SamAccountName")
                    {
                        $name = "__UID__"
                        $resultMap.add("$name", "$value")
                        $name = "__NAME__"
                        $resultMap.add("$name", "$value")
                    }
                }
            }
            $resultList += $resultMap
        }
    }

    # convert to json
    ConvertTo-Json $resultList
}
catch [Microsoft.ActiveDirectory.Management.ADIdentityNotFoundException] {
    Write-Host "User does not exists"
    # convert to json
    ConvertTo-Json $resultList
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Search ended"
