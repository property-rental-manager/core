#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(
  cd "$(dirname "${BASH_SOURCE[0]}")/.."
  pwd
)"

docker compose \
  --env-file "$ROOT_DIR/.env" \
  --file "$ROOT_DIR/infra/docker/compose.yaml" \
  down