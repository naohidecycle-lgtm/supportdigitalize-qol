#!/bin/bash
# Prism GET専用モック起動スクリプト
# 使い方: ./mock-get.sh [port]
set -euo pipefail

PORT="${1:-4010}"
SPEC="qol_weekly_get.yaml"

cd "$(dirname "$0")"

# 1) 前提チェック
if ! command -v prism >/dev/null 2>&1; then
  echo "❌ Prism が見つかりません。次でインストールしてください:"
  echo "   npm install -g @stoplight/prism-cli"
  exit 1
fi

if [ ! -f "$SPEC" ]; then
  echo "❌ Spec ファイルが見つかりません: $SPEC"
  echo "   カレント: $(pwd)"
  exit 1
fi

echo "🚀 Starting Prism mock server (GET-only)"
echo "📄 Spec: $SPEC"
echo "🌐 Port: $PORT"
echo "------------------------------------"

exec prism mock "./$SPEC" -p "$PORT"
