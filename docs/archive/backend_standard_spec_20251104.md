---
title: "SupportDigitalize Backend (Standard) — 2025-11-04"
version: "2025-11-04"
---

# Backend Standard（2025-11-04保存版）

了解しました。以下は `docs/archive/backend_standard_spec_20251104.md` にそのまま貼り付けられる
**「Backend Standard（2025-11-04 保存版）」** の正式本文です。
（既に Material for MkDocs は動作しているので、この内容を入れて再デプロイすれば綺麗に表示されます。）

---

````markdown
---
title: "SupportDigitalize Backend (Standard)"
version: "2025-11-04"
author: "Naohide Yahagi"
project: "SupportDigitalize QOL System"
description: "AWS Glue / Lambda / EventBridge / DynamoDB architecture and standard workflow (archived 2025-11-04)"
---

# 🏗️ SupportDigitalize Backend Standard Documentation（2025-11-04 保存版）

## 1. 目的と概要
本ドキュメントは **QOL支援システム（SupportDigitalize）** のバックエンド標準構成を 2025-11-04 時点の状態で保存したものであり、  
開発・運用・保守の基盤設計を統一し、後続フェーズに引き継ぐことを目的とする。

主な要素:
- Garmin Health / Activity API からの自動データ取得  
- ETL による週次メトリクス生成と Athena での分析  
- EventBridge による自動スケジュール実行  
- Lambda による Glue 起動制御・SNS通知  
- DynamoDB による ID マッピング管理

---

## 2. 全体構成（System Overview）

```mermaid
graph TD
  A[Garmin Cloud (Webhook / Pull API)] -->|push/ping-pull| B[Lambda: garmin_webhook_receiver]
  B --> C[S3: raw/daily/]
  C --> D[AWS Glue Job: qol-weekly-etl]
  D --> E[S3: processed/weekly/]
  E --> F[Athena: db_qol.weekly_metrics]
  F --> G[Dashboard / Analytics]
  D -.-> H[DynamoDB: user_id_map]
  H -.-> B
  I[EventBridge Scheduler (水曜09:05 JST)] -->|Trigger| J[Lambda: qol-weekly-survey-kick]
  J -->|Start| D
  D -->|SNS Notify| K[SNS: job_failure_alert]
````

---

## 3. AWS コンポーネント一覧

| コンポーネント                             | 概要            | 主設定項目                                            |
| ----------------------------------- | ------------- | ------------------------------------------------ |
| **S3 (raw/daily)**                  | Garminデータ日次保存 | JSONL形式 / prefix=`raw/daily/date=YYYY-MM-DD/`    |
| **S3 (processed/weekly)**           | ETL後の週次結果     | Parquet形式 / partition=`date`                     |
| **Glue Job: qol-weekly-etl**        | ETL本体         | PySpark / safe_get実装済み / dynamic partition write |
| **Lambda: garmin_webhook_receiver** | Webhook受信     | API Gateway経由 / 30秒以内HTTP200応答                   |
| **Lambda: qol-weekly-survey-kick**  | ETL起動         | EventBridgeトリガーをGlueへ転送                          |
| **DynamoDB: user_id_map**           | ID変換管理        | PK: garmin_user_id                               |
| **SNS: job_failure_alert**          | 障害通知          | 管理者メール購読                                         |
| **EventBridge Scheduler**           | 定期実行          | cron(5 0 ? * WED *)                              |

---

## 4. データフロー（ETL処理）

### 4.1 Ingest

* Garmin API より push 通知受信
* Lambda → S3 `/raw/daily/` へ書込み

### 4.2 Transform

* Glue Job にて週次集計

  * `safe_get()` により欠損・型揺れ対応
  * `pathGlobFilter("*.jsonl")` で入力制御
  * date 欠損時は proc_date を注入

### 4.3 Load

* 出力: Parquet (`s3://.../processed/weekly/date=YYYY-MM-DD/`)
* Athena テーブル: `db_qol.weekly_metrics`
* ダッシュボードから可視化

---

## 5. 運用設計

| 項目          | 内容                                                                         |
| ----------- | -------------------------------------------------------------------------- |
| **スケジュール**  | EventBridge → Lambda → Glue                                                |
| **再試行ポリシー** | Glue: MaxAttempts=2 / Timeout=60min                                        |
| **ロギング**    | CloudWatch Logs (`/aws-glue/jobs/output`)                                  |
| **通知**      | SNSトピック（失敗時メール）                                                            |
| **データ確認**   | Athena: `SELECT * FROM db_qol.weekly_metrics ORDER BY date DESC LIMIT 10;` |
| **バックアップ**  | S3 Versioning + ZIP保全                                                      |

---

## 6. セキュリティ

| 分類            | 内容                                      |
| ------------- | --------------------------------------- |
| **IAM最小権限**   | Lambda / Glue / EventBridge / SNS ごとに分離 |
| **KMS暗号化**    | S3 / DynamoDB / SNS へ適用                 |
| **Secrets管理** | Garmin API認証情報は Secrets Manager へ       |
| **ログ保全**      | CloudWatch Logs 保持180日                  |
| **Webhook応答** | 常に30秒以内にHTTP200                         |

---

## 7. デプロイ手順（概要）

```bash
# 1) ローカル修正
vim data/glue_scripts/qol_weekly_etl.py

# 2) S3へアップロード
aws s3 cp data/glue_scripts/qol_weekly_etl.py \
  s3://supportdigitalize-data-304838292017-ap-northeast-1/scripts/

# 3) Glueジョブ更新
aws glue update-job --job-name qol-weekly-etl \
  --job-update '{
    "Command": {"Name": "glueetl",
    "ScriptLocation": "s3://supportdigitalize-data-304838292017-ap-northeast-1/scripts/qol_weekly_etl.py"}
  }'
```

---

## 8. 今後の拡張計画

| フェーズ    | 内容                    |
| ------- | --------------------- |
| Phase 1 | Garmin API連携＋週次ETL安定化 |
| Phase 2 | WHOQOL-26＋センサ統合分析     |
| Phase 3 | 行動推奨（LLM/強化学習）        |
| Phase 4 | 端末内学習（Federated / DP） |
| Phase 5 | 自治体導入・実証実験            |

---

## 9. 改訂履歴

| 日付         | バージョン | 内容                | 編集者            |
| ---------- | ----- | ----------------- | -------------- |
| 2025-11-04 | v1.0  | 保存版作成（Standard仕様） | Naohide Yahagi |

---

> 📘 **保存情報**
> 本ドキュメントは 2025-11-04 時点の仕様書アーカイブです。
> 最新版は [`docs/backend_standard_spec.md`](../backend_standard_spec.md) を参照してください。

````

---

## ✅ 貼り付け手順

```bash
nano docs/archive/backend_standard_spec_20251104.md
# ← 上記全文を貼り付け、Ctrl+O → Enter → Ctrl+X で保存
````

次に再ビルド＆公開：

```bash
mkdocs serve -a 127.0.0.1:8000
# → 表示確認後 Ctrl+C
git add docs/archive/backend_standard_spec_20251104.md
git commit -m "docs: add backend standard spec (2025-11-04 保存版)"
git push
mkdocs gh-deploy --force
```

ブラウザで
[https://naohidecycle-lgtm.github.io/supportdigitalize-qol/](https://naohidecycle-lgtm.github.io/supportdigitalize-qol/)
を再読み込みすると、メニュー「Backend → Archive → 2025-11-04」で本文が表示されます。

