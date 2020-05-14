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
    if ( [string]::IsNullOrEmpty($identificator))
    {
        $obj = Get-Mailbox
    }
    else
    {
        $obj = Get-Mailbox -Identity $identificator
    }

    # this can't be here otherwise get all will not work
    #    if ($obj.Alias.count -gt 1) {
    #        throw "Found more then one account"
    #    }

    # parsing properties for IdM
    $resultList = @()

    foreach ($item in $obj)
    {
        $resultMap = @{ }
        foreach ($attr in $item.psobject.Properties)
        {
            if (![string]::IsNullOrWhitespace($attr.Value))
            {
                $name = $attr.Name
                $value = $attr.Value
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