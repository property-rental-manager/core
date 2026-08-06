#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

ENV_FILE="$ROOT_DIR/.env"
COMPOSE_FILE="$ROOT_DIR/infra/docker/compose.dev.yaml"

if [ ! -f "$ENV_FILE" ]; then
    ENV_FILE="$ROOT_DIR/.env.example"
fi

FORCE=false
if [ "${1:-}" = "-f" ] || [ "${1:-}" = "--force" ]; then
    FORCE=true
fi

echo "=========================================================="
echo "WARNING: Resetting the development database!"
echo "This will destroy all local database data in the container volume."
echo "=========================================================="

if [ "$FORCE" = false ]; then
    read -rp "Are you sure you want to proceed? Type 'RESET' to confirm: " CONFIRMATION
    if [ "$CONFIRMATION" != "RESET" ]; then
        echo "Database reset cancelled."
        exit 0
    fi
fi

echo "Stopping containers and removing project PostgreSQL volume..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" down -v

echo "Re-starting database with clean volume..."
"$SCRIPT_DIR/dev-db-up.sh"
