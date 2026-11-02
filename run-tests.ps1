param([string]$Tags = "not @defeito")

Set-Location -Path $PSScriptRoot

$encodingAnterior = [Console]::OutputEncoding
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

$composeFile = "docker-compose.test.yml"
$relatorios  = "ecocompliance-tests\reports"
$env:CUCUMBER_FILTER_TAGS = $Tags

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: o Docker nao respondeu. Abra o Docker Desktop, espere aparecer 'Engine running' e rode de novo." -ForegroundColor Red
    [Console]::OutputEncoding = $encodingAnterior
    exit 1
}

New-Item -ItemType Directory -Force -Path $relatorios | Out-Null
Get-ChildItem -Path $relatorios -File -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue

$argumentos = @("compose", "-f", $composeFile, "up", "--build")
$ajuda = (docker compose up --help) | Out-String
if ($ajuda -match "--attach ") { $argumentos += @("--attach", "api-tests") }
$argumentos += @("--abort-on-container-exit", "--exit-code-from", "api-tests")

Write-Host "==> Filtro de tags: $Tags"
Write-Host "==> Subindo Oracle XE + EcoCompliance API e executando os testes..."
Write-Host "    (a primeira execucao baixa as imagens e as dependencias e pode levar de 5 a 10 minutos)"

& docker @argumentos
$status = $LASTEXITCODE

if ($status -ne 0) {
    Write-Host ""
    Write-Host "==> Ultimas linhas do log da API (diagnostico):"
    docker compose -f $composeFile logs --tail 60 ecocompliance-api
}

Write-Host "==> Derrubando o ambiente de testes..."
docker compose -f $composeFile down -v --remove-orphans *> $null

Write-Host ""
if ($status -eq 0) {
    Write-Host "[OK] Suite executada com sucesso." -ForegroundColor Green
} else {
    Write-Host "[FALHA] Suite finalizada com falhas (codigo $status)." -ForegroundColor Red
}
Write-Host "Relatorio HTML: $relatorios\cucumber.html"

[Console]::OutputEncoding = $encodingAnterior
exit $status
