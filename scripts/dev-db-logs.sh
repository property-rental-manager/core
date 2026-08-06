#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

ENV_FILE="$ROOT_DIR/.env"
COMPOSE_FILE="$ROOT_DIR/infra/docker/compose.dev.yaml"

if [ ! -f "$ENV_FILE" ]; then
    ENV_FILE="$ROOT_DIR/.env.example"
fi

echo "Streaming logs for service 'db'..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f db
