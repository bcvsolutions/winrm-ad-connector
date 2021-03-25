# Update script, which will create or update mailbox
# INPUT:
#   uid - String - account identifier
#   Database - String - name of database for mailbox creation
#   PrimarySmtpAddress - String - email address

function updateOrCreateMailbox
{
    # Enable mailbox for existing user, or update it
    $User = Get-ADUser -Identity $uid
    if ($Null -ne $User) {
        $mailbox = Get-Mailbox -Identity $uid
        $mail = "$PrimarySmtpAddress"
        if ("" -eq $mail) {
            Write-Host "Didn't get any email from IdM for $uid, nothing to do"
        } else {
            if ($Null -ne $mailbox) {
                Write-Host "Update mailbox for user $uid with PrimarySmtpAddress $mail"
                $mailbox = Set-Mailbox -Identity $uid -PrimarySmtpAddress "$mail" -ea Stop
                Write-Host "Mailbox for user $uid updated"
            } else {
                Write-Host "Create mailbox for user $uid with PrimarySmtpAddress $mail"
                $mailbox = Enable-Mailbox -Identity $uid -Database "$Database" -PrimarySmtpAddress "$mail" -ea Stop
                Write-Host "Mailbox for user $uid created"
            }
        }
    } else {
        throw "User $uid has no AD account, we can't assign mailbox"
    }
}


Write-Host "PS Update started"
#Needed to load Exchange cmdlets - the alternative would be connecting by Remote PowerShell
Add-PSSnapin -Name '*Exchange*'
try
{
    # An alternative to loading Exchange cmdlets - connecting by Remote PowerShell 
    #$Session = New-PSSession -ConfigurationName Microsoft.Exchange -ConnectionUri "http://exchangeserver.domain.tld/PowerShell/" -Authentication Kerberos
    #Write-Host "PS Session started"
    # Import session and its cmdlets. The result is redirected to output variable so it doesn't get to StdOut.
    #$output = Import-PSSession $Session -DisableNameChecking

    updateOrCreateMailbox
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
Write-Host "PS Update ended"
