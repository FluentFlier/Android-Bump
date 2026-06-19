#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/backend"

if ! command -v npx >/dev/null 2>&1; then
  echo "Node.js/npx required. Install Node 20+ first."
  exit 1
fi

npm install

if ! npx wrangler whoami 2>&1 | grep -q "You are logged in"; then
  echo "Run: npx wrangler login"
  echo "Then re-run: ./scripts/deploy-backend.sh"
  exit 1
fi

if grep -q "REPLACE_WITH_KV_NAMESPACE_ID" wrangler.toml; then
  echo "Creating KV namespace..."
  KV_ID=$(npx wrangler kv namespace create PROFILES | awk '/id =/ {print $3}')
  sed -i.bak "s/REPLACE_WITH_KV_NAMESPACE_ID/$KV_ID/" wrangler.toml
  rm -f wrangler.toml.bak
  echo "Updated wrangler.toml with KV id: $KV_ID"
fi

if grep -q "YOUR_SUBDOMAIN" wrangler.toml; then
  echo "Set PUBLIC_BASE_URL in backend/wrangler.toml before deploy."
  exit 1
fi

npm run deploy

echo ""
echo "Backend deployed. Update DEFAULT_BASE_URL in app/build.gradle.kts to match PUBLIC_BASE_URL."
