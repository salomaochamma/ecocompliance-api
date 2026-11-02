#!/usr/bin/env bash
set -uo pipefail

cd "$(dirname "$0")"

COMPOSE_FILE="docker-compose.test.yml"
RELATORIOS="ecocompliance-tests/reports"
export CUCUMBER_FILTER_TAGS="${1:-not @defeito}"

if ! docker compose version >/dev/null 2>&1; then
  echo "ERRO: 'docker compose' não encontrado. Instale o Docker com o plugin Compose v2." >&2
  exit 1
fi

compose() { docker compose -f "$COMPOSE_FILE" "$@"; }

mkdir -p "$RELATORIOS"
rm -f "$RELATORIOS"/* 2>/dev/null || true

ATTACH=""
if docker compose up --help 2>/dev/null | grep -q -- "--attach "; then
  ATTACH="--attach api-tests"
fi

echo "==> Filtro de tags: $CUCUMBER_FILTER_TAGS"
echo "==> Subindo Oracle XE + EcoCompliance API e executando os testes..."
echo "    (a primeira execução baixa as imagens e as dependências e pode levar alguns minutos)"

compose up --build $ATTACH --abort-on-container-exit --exit-code-from api-tests
STATUS=$?

if [ "$STATUS" -ne 0 ]; then
  echo
  echo "==> Últimas linhas do log da API (diagnóstico):"
  compose logs --tail 60 ecocompliance-api || true
fi

echo "==> Derrubando o ambiente de testes..."
compose down -v --remove-orphans >/dev/null 2>&1 || true

echo
if [ "$STATUS" -eq 0 ]; then
  echo "[OK] Suíte executada com sucesso."
else
  echo "[FALHA] Suíte finalizada com falhas (código $STATUS)."
fi
echo "Relatório HTML: $RELATORIOS/cucumber.html"
exit "$STATUS"
