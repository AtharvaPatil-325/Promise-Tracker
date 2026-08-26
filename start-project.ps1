$repo = 'C:\Users\ankii\Promise'
$backendDir = Join-Path $repo 'backend'
$frontendDir = Join-Path $repo 'frontend'
$mvnPath = Join-Path $HOME 'maven\apache-maven-3.9.16\bin\mvn.cmd'

$portProcess = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique

if ($portProcess) {
    Write-Host "Stopping process using port 8080: $($portProcess -join ', ')"
    foreach ($pid in $portProcess) {
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "Starting backend..."
$backendJob = Start-Job -Name PromiseBackend -ScriptBlock {
    param($repo, $mvnPath)
    Set-Location $repo
    & $mvnPath -f backend spring-boot:run
} -ArgumentList $repo, $mvnPath

Start-Sleep -Seconds 8

Write-Host "Starting frontend..."
$frontendJob = Start-Job -Name PromiseFrontend -ScriptBlock {
    param($repo)
    Set-Location $repo
    npm run dev --prefix frontend
} -ArgumentList $repo

Write-Host "Project startup initiated."
Write-Host "Backend logs: Get-Content -Wait (Get-ChildItem $env:USERPROFILE\AppData\Local\Temp -Filter '*copilot*' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime | Select-Object -Last 1).FullName"
Write-Host "Frontend URL: http://localhost:3003"
Write-Host "Backend URL: http://localhost:8080"
Write-Host "Swagger URL: http://localhost:8080/api/v1/swagger-ui/index.html"
