param(
    [switch]$Docker
)

$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

if ($env:JAVA_HOME) {
    $env:JAVA_HOME = $env:JAVA_HOME.TrimEnd('\')
}

$mavenCommand = if (Test-Path -Path (Join-Path $PSScriptRoot '.mvn\wrapper\maven-wrapper.properties')) {
    '.\mvnw.cmd'
} else {
    'mvn'
}
$mavenArgs = '-Dmaven.repo.local=.m2'

$shellCommand = if (Get-Command pwsh -ErrorAction SilentlyContinue) {
    'pwsh'
} elseif (Get-Command powershell -ErrorAction SilentlyContinue) {
    'powershell'
} else {
    (Get-Process -Id $PID).Path
}

if ($Docker) {
    & $mavenCommand $mavenArgs clean package
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
    docker compose up --build
    exit $LASTEXITCODE
}

& $mavenCommand $mavenArgs clean package
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$services = @(
    @{ Name = 'auth-service'; Module = 'auth-service'; Port = 8081 },
    @{ Name = 'user-service'; Module = 'user-service'; Port = 8082 },
    @{ Name = 'client-service'; Module = 'client-service'; Port = 8083 },
    @{ Name = 'api-gateway'; Module = 'api-gateway'; Port = 3000 }
)

foreach ($svc in $services) {
    $cmd = "Set-Location -Path '$PSScriptRoot'; $mavenCommand $mavenArgs -pl $($svc.Module) spring-boot:run"
    Start-Process $shellCommand -ArgumentList '-NoExit', '-Command', $cmd | Out-Null
}

Write-Host 'Started services in separate windows:'
foreach ($svc in $services) {
    Write-Host "- $($svc.Name): http://localhost:$($svc.Port)"
}
