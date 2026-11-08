$ErrorActionPreference = 'Stop'
$portRoot = Split-Path $PSScriptRoot -Parent
$peerRoot = Split-Path $portRoot -Parent
foreach ($repo in @('consecration', 'demolition-order', 'weighted_armors')) {
    $repoPath = Join-Path $peerRoot $repo
    & (Join-Path $repoPath 'gradlew.bat') -p $repoPath build --console=plain
    if ($LASTEXITCODE -ne 0) { throw "Build failed: $repo" }
}
$runPath = Join-Path $PSScriptRoot 'run'
New-Item -ItemType Directory -Path $runPath -Force | Out-Null
Set-Content -LiteralPath (Join-Path $runPath 'eula.txt') -Value 'eula=true'
$props = @('online-mode=false', 'server-ip=127.0.0.1', 'server-port=25587', 'view-distance=2', 'simulation-distance=2')
Set-Content -LiteralPath (Join-Path $runPath 'server.properties') -Value $props
if (Test-Path -LiteralPath (Join-Path $runPath 'port-test-result.txt')) { Remove-Item -LiteralPath (Join-Path $runPath 'port-test-result.txt') }
& (Join-Path $portRoot 'gradlew.bat') -p $PSScriptRoot runServer --console=plain
if ($LASTEXITCODE -ne 0 -or (Get-Content (Join-Path $runPath 'port-test-result.txt') -Raw) -notmatch '^PASS 22 checks') { throw 'Server smoke test failed' }
$clientWorld = Join-Path $runPath 'saves/PortSmokeWorld'
if (-not (Test-Path -LiteralPath $clientWorld)) {
    New-Item -ItemType Directory -Path (Split-Path $clientWorld -Parent) -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $runPath 'world') -Destination $clientWorld -Recurse
}
if (Test-Path -LiteralPath (Join-Path $runPath 'port-client-result.txt')) { Remove-Item -LiteralPath (Join-Path $runPath 'port-client-result.txt') }
& (Join-Path $portRoot 'gradlew.bat') -p $PSScriptRoot runClient --console=plain
if ($LASTEXITCODE -ne 0 -or (Get-Content (Join-Path $runPath 'port-client-result.txt') -Raw) -notmatch '^PASS') { throw 'Client smoke test failed' }
Write-Host 'Passed: server and client port smoke tests.'
