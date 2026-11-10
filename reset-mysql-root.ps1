# Reset MySQL 9.6 root password (v3: init-file inside Data dir + icacls grant)
# Run this script AS ADMINISTRATOR.
$ErrorActionPreference = "Continue"

$ServiceName = "MySQL96"
$Mysql       = "D:\Program Files\MySQL\MySQL Server 9.6\bin\mysql.exe"
$IniFile     = "C:\ProgramData\MySQL\MySQL Server 9.6\my.ini"
$IniBackup   = "C:\ProgramData\MySQL\MySQL Server 9.6\my.ini.bak"
$InitSql     = "C:\ProgramData\MySQL\MySQL Server 9.6\Data\reset_root_pwd.sql"
$NewPassword = "root123456"

Write-Host "=== [1/7] Write init SQL into MySQL Data dir ==="
@"
ALTER USER 'root'@'localhost' IDENTIFIED BY '$NewPassword';
"@ | Out-File -FilePath $InitSql -Encoding ascii
icacls $InitSql /grant "Everyone:R" | Out-Null
Write-Host "      init SQL -> $InitSql  (Everyone:R granted)"

Write-Host "=== [2/7] Backup and patch my.ini ==="
Copy-Item $IniFile $IniBackup -Force
Add-Content -Path $IniFile -Value "`ninit-file=`"C:/ProgramData/MySQL/MySQL Server 9.6/Data/reset_root_pwd.sql`""
Write-Host "      backup -> $IniBackup"

try {
    Write-Host "=== [3/7] Stop service ==="
    Stop-Service -Name $ServiceName -Force
    Start-Sleep -Seconds 5

    Write-Host "=== [4/7] Start service (apply init-file) ==="
    Start-Service -Name $ServiceName -ErrorAction Stop
    Write-Host "      service started, waiting for init-file to apply..."
    Start-Sleep -Seconds 12
} catch {
    Write-Host "!!! Start failed. Latest MySQL error log:"
    $errFiles = Get-ChildItem "C:\ProgramData\MySQL\MySQL Server 9.6\Data" -Filter "*.err" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending
    if ($errFiles) { Get-Content $errFiles[0].FullName -Tail 25 }
} finally {
    Write-Host "=== [5/7] Restore my.ini ==="
    Copy-Item $IniBackup $IniFile -Force
    Write-Host "      my.ini restored from backup"
}

Write-Host "=== [6/7] Ensure service is running (clean config) ==="
$svc = Get-Service -Name $ServiceName
if ($svc.Status -ne "Running") {
    Stop-Service -Name $ServiceName -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 5
    Start-Service -Name $ServiceName -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 8
}
$svcAfter = Get-Service -Name $ServiceName
Write-Host "      service status: $($svcAfter.Status)"

Write-Host ""
Write-Host "=== [7/7] Verify ==="
& $Mysql -h 127.0.0.1 -P 3306 -u root "--password=$NewPassword" -e "SELECT VERSION() AS version;"
Write-Host ""
Write-Host "=== DONE. New root password: $NewPassword ==="
