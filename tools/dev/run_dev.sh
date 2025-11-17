#!/usr/bin/env bash
set -euo pipefail

# ============================================
# SupportDigitalize 開発用 一発起動スクリプト
# Prism + Android + Logcat をまとめて起動
# ============================================

# プロジェクトルート（このスクリプトから見て 2 つ上）
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PRISM_DIR="$ROOT/tools/mock"
MOBILE_DIR="$ROOT/frontend/mobile"

echo "== SupportDigitalize Android MVP 起動スクリプト =="
echo "ROOT: $ROOT"
echo

# --------------------------------------------
# 1. Prism モックサーバを再起動
# --------------------------------------------
echo "[1/4] Prism Mock を port 4010 で再起動します..."

cd "$PRISM_DIR"

# 既存の Prism プロセスを終了（あれば）
pkill -f "prism mock .*4010" 2>/dev/null || true

# バックグラウンドで起動（ログは prism.log に保存）
nohup prism mock ./qol_weekly_api.yaml -p 4010 -h 127.0.0.1 \
  > "$PRISM_DIR/prism.log" 2>&1 &

echo "  → Prism 起動コマンド送信済み。（ログ: $PRISM_DIR/prism.log）"
echo

# --------------------------------------------
# 2. Android アプリのビルド
# --------------------------------------------
echo "[2/4] Android アプリを assembleDebug します..."

cd "$MOBILE_DIR"
./gradlew assembleDebug

echo "  → assembleDebug 完了"
echo

# --------------------------------------------
# 3. APK インストール & MainActivity 起動
# --------------------------------------------
echo "[3/4] APK をインストールして MainActivity を起動します..."

adb -e uninstall com.sd.mobile || true
adb -e install -r app/build/outputs/apk/debug/app-debug.apk

echo "  → インストール完了。MainActivity を起動します..."
echo

adb -e shell am start -S -W \
  -a android.intent.action.MAIN \
  -c android.intent.category.LAUNCHER \
  -n com.sd.mobile/.MainActivity

echo
echo "  → MainActivity 起動リクエスト完了"
echo

# --------------------------------------------
# 4. logcat 監視（フィルタ付き）
# --------------------------------------------
echo "[4/4] logcat を監視します（Ctrl+C で終了）"
echo "  フィルタ: AndroidRuntime / FATAL / OkHttp / Weekly / WeeklyRepo / WeeklyVM"
echo

set +e
adb -e logcat | grep -E "AndroidRuntime|FATAL|OkHttp|Weekly|WeeklyRepo|WeeklyVM"
