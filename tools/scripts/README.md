# 🧰 Tools / Scripts Overview

## Mock Server（Prism）
| Script        | Purpose                         | Usage               |
|---------------|----------------------------------|---------------------|
| `mock-get.sh` | GET専用モック（UI安定デモ向け） | `./mock-get.sh`     |
| `mock-all.sh` | フルspecモック（E2E確認向け）   | `./mock-all.sh`     |

- デフォルトのポートは `4010`。変更したい場合は引数で指定（例：`./mock-get.sh 4020`）。
- Android エミュレータからアクセスする場合は `http://10.0.2.2:<port>` を使用。
- Prism が無い場合は `npm install -g @stoplight/prism-cli` でインストールしてください。
