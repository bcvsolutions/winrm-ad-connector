# Delete script, which will delete exchange account and user in AD will still remain
# INPUT:
#   uid - String - account identifier

Write-Host "PS Delete started"
#Needed to load Exchange cmdlets - the alternative would be connecting by Remote PowerShell
Add-PSSnapin -Name '*Exchange*'
try
{
    # An alternative to loading Exchange cmdlets - connecting by Remote PowerShell 
    #$Session = New-PSSession -ConfigurationName Microsoft.Exchange -ConnectionUri "http://exchangeserver.domain.tld/PowerShell/" -Authentication Kerberos
    #Write-Host "PS Session started"
    # Import session and its cmdlets. The result is redirected to output variable so it doesn't get to StdOut.
    #$output = Import-PSSession $Session -DisableNameChecking
    $mailbox = Disable-Mailbox -Identity $uid -Confirm:$false -ea Stop
    Write-Host "Mailbox deleted"
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
finally
{
    # Close Remote PowerShell session
    #Remove-PSSession $Session
}
Write-Host "PS Delete ended"
