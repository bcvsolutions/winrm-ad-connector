# Create script, which will create exchange account for existing user in AD
# INPUT:
#   uid - String - account identifier
#   Database - String - name of database for mailbox creation
#   PrimarySmtpAddress - String - email address

Write-Host "PS Create started"
#Needed to load Exchange cmdlets - the alternative would be connecting by Remote PowerShell
Add-PSSnapin -Name '*Exchange*'
try
{
    # An alternative to loading Exchange cmdlets - connecting by Remote PowerShell
    #$Session = New-PSSession -ConfigurationName Microsoft.Exchange -ConnectionUri "http://exchangeserver.domain.tld/PowerShell/" -Authentication Kerberos
    #Write-Host "PS Session started"
    # Import session and its cmdlets. The result is redirected to output variable so it doesn't get to StdOut.
    #$output = Import-PSSession $Session -DisableNameChecking

    # Enable mailbox for existing user
    $User = Get-ADUser -Identity $uid
    if ($Null -ne $User) {
        Write-Host "Create mailbox for user $uid with PrimarySmtpAddress $PrimarySmtpAddress"
        $mailbox = Enable-Mailbox -Identity $uid -Database "$Database" -PrimarySmtpAddress "$PrimarySmtpAddress" -ea Stop
        Write-Host "Mailbox for user $uid created"
    } else {
        throw "User $uid has no AD account, we can't assign mailbox"
    }
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
Write-Host "PS Create ended"
