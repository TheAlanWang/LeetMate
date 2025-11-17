#!/usr/bin/env bash
set -euo pipefail

# Auto-load local env vars if present (e.g., database creds, seed flag).
if [[ -f .env ]]; then
  set -a
  source .env
  set +a
fi

./mvnw spring-boot:run "$@"
