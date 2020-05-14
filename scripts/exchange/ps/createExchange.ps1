# Create script, which will create exchange account for existing user in AD or create user during it
# INPUT:
#   uid - String - account identifier

Write-Host "PS Create started"
#Needed to load Exchange cmdlets
Add-PSSnapin -Name '*Exchange*'
try
{
    # Enable mailbox for existing user
    #$mailbox = Enable-Mailbox -Identity $uid
    #creating exchange with ad account on test server
    $mailbox = New-Mailbox -SamAccountName $uid -UserPrincipalName bcvvytvoren@adradic2.lopaticka.piskoviste.bcv -Alias vytvoren -Name BcvVytvoren -OrganizationalUnit 'CN=Users,DC=lopaticka,DC=piskoviste,DC=bcv' -Password (ConvertTo-SecureString -String 'Demo1234' -AsPlainText -Force) -FirstName bcv -LastName vytvoren -DisplayName "Bcv Vytvoren"

    # set additional attributes to AD
	$User = Get-ADUser -Identity $mailbox.SamAccountName -Properties legacyExchangeDN
    if ($User -ne $Null) {
        Write-Host "Updating additional attributes"
        Set-ADUser -Identity $User.SamAccountName `
            -Confirm:$false `
            -Title "$position" `
            -Description "$position" `
            -Department "$department" `
            -Office "$depo"
    }
}
catch
{
    # TODO implement rollback in case something failed
    # Write to stderr and exit with code 1
    [Console]::Error.WriteLine($_.Exception)
    exit 1
}
Write-Host "PS Create ended"