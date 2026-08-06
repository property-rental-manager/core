#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

ENV_FILE="$ROOT_DIR/.env"
COMPOSE_FILE="$ROOT_DIR/infra/docker/compose.dev.yaml"

if [ ! -f "$ENV_FILE" ]; then
    echo "Warning: .env file not found at $ENV_FILE. Copying from .env.example..."
    cp "$ROOT_DIR/.env.example" "$ENV_FILE"
fi

echo "Starting PostgreSQL database container..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d db

echo "Waiting for PostgreSQL healthcheck..."
MAX_WAIT=30
WAIT_COUNT=0

until [ "$(docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps db --format '{{.Health}}')" = "healthy" ]; do
    WAIT_COUNT=$((WAIT_COUNT + 1))
    if [ "$WAIT_COUNT" -ge "$MAX_WAIT" ]; then
        echo "Error: Database healthcheck timed out after ${MAX_WAIT}s."
        docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs db
        exit 1
    fi
    sleep 1
done

PORT=$(grep '^POSTGRES_PORT=' "$ENV_FILE" | cut -d '=' -f 2 || echo "5432")
DB_NAME=$(grep '^POSTGRES_DB=' "$ENV_FILE" | cut -d '=' -f 2 || echo "property_rental_manager")

echo "=========================================================="
echo "PostgreSQL is healthy and ready for connections!"
echo "Host:     localhost"
echo "Port:     ${PORT:-5432}"
echo "Database: ${DB_NAME:-property_rental_manager}"
echo "=========================================================="
