# Create script, which will create a new distribution group
# INPUT:
#   uid - String - account identifier

Write-Host "PS Create dist. group started"
#Needed to load Exchange cmdlets
Add-PSSnapin -Name '*Exchange*'
try
{
    New-DistributionGroup -Name "$name" -OrganizationalUnit "$organizationalunit" -SamAccountName "$name" -Type "Distribution" -PrimarySmtpAddress "$mail" -DisplayName "$displayName" -MemberDepartRestriction closed -MemberJoinRestriction closed

}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Create dist. group ended"

