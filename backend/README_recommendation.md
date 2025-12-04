## 技術サマリー README（修正版フル出力）

ここから下は、**GitHub の `README.md` としてそのまま保存できる形**で再構成した最新版です。

（Body Battery / QOLスコア計算ロジック / 推奨ロジックをすべて反映）

---

```markdown
# 📘 SupportDigitalize QOL 推奨システム — 技術サマリー（エンジニア向け）

本ドキュメントは、SupportDigitalize の **Weekly 推奨機能**に関する技術概要を整理し、

- データの発生源
- 送信・保管・処理によるデータ形式の変化
- 推奨行動（Recommendation）の決定ロジック

をエンジニア向けに明文化したものです。
ゼミ共有 / 外部エンジニアへの説明 / 将来の実装変更の基盤として利用します。

---

## 🗺️ 1. 全体アーキテクチャ

```mermaid
flowchart TD

    subgraph UserDevice["📱 Android (Jetpack Compose)"]
        A1[WeeklyScreen<br/>(Weekly + Recommendations)]
        A2[RecommendationViewModel]
        A3[QOL SurveyScreen]
    end

    subgraph Backend["🖥️ FastAPI Backend"]
        B1[/GET /qol/weekly/]
        B2[/POST /qol/survey/]
        B3[/POST /recommendations/]
        B4[Recommendation Engine (engine.py)]
    end

    subgraph AWS["☁️ AWS Cloud"]
        C1[(DynamoDB: QOL Survey)]
        C2[(DynamoDB: Weekly Metrics)]
        C3[(S3: Raw Activity Data)]
        C4[(Glue ETL)]
    end

    %% Weekly
    A1 -->|GET| B1
    B1 -->|Weekly JSON| A1

    %% Recommendation
    A1 -->|Request Recommend| A2
    A2 -->|POST /recommendations| B3
    B3 --> B4
    B4 -->|Recommendation JSON| A2
    A2 -->|UiModel| A1

    %% Survey
    A3 -->|POST /qol/survey| B2
    B2 -->|write| C1

    %% ETL
    C3 --> C4 --> C2

```

---

## 📦 2. データ発生源と用途

| 種類 | 発生場所 | 内容 | 主な用途 |
| --- | --- | --- | --- |
| 行動データ（Raw） | Garmin / Android | 歩数・睡眠・Body Battery 等 | Weekly 指標の算出 |
| Weekly 指標 | Glue ETL | 週単位の要約指標 | 推奨ロジックの入力 / Weekly UI |
| QOL サーベイ（7問） | Android | q1〜q7（1〜5点） | 4領域QOLスコア算出 / 推奨ロジック |
| 推奨行動（Recommendation） | Backend | ルールベースで生成 | WeeklyScreen に表示 |

---

## 🧪 3. 行動データ（Garmin → S3 → Glue → DynamoDB）

### 3.1 Raw データ（Garmin 側）

※ Body Battery を含めた将来の想定フォーマット例：

```json
{
  "userId": "demo-user",
  "timestamp": "2025-11-17T08:00:00Z",
  "steps": 542,
  "sleep_seconds": 28000,
  "stress_level": 45,
  "body_battery": 62
}

```

- `body_battery`: 0〜100 の指標を想定
- Garmin Developer Portal で、Body Battery を含むカテゴリを許可することで取得可能になる想定

---

### 3.2 S3 への保存

- 日付パーティション付きで保存
    - 例：`s3://.../year=2025/month=11/day=17/hour=08/...`
- JSON or gz 圧縮形式

---

### 3.3 Glue ETL による Weekly 指標への変換

Glue ジョブでは、日単位の Raw データを週単位に集約し、以下のようなレコードを生成します。

```json
{
  "user_id": "demo-user",
  "week_key": "2025-W48",
  "total_steps": 31200,
  "avg_sleep_hours": 7.1,
  "evening_walk_minutes": 54,
  "rhythm_score": 0.78,
  "avg_body_battery": 65.3,
  "updated_at": "2025-11-24T02:10:00Z"
}

```

- `avg_body_battery` は、`body_battery` の日次平均をさらに週次平均した指標（将来追加）
- このレコードは **DynamoDB: Weekly Metrics テーブル (`C2`)** に保存されます。

---

## 🧾 4. QOL サーベイ（7問 → 4領域スコア）

### 4.1 Android からの入力形式

```json
{
  "user_id": "demo-user",
  "q1": 4,
  "q2": 3,
  "q3": 5,
  "q4": 2,
  "q5": 4,
  "q6": 3,
  "q7": 5
}

```

- 各項目は 1〜5 点
- 質問の設計は WHOQOL-26 を参考に、4領域（身体・心理・社会・環境）を7問に圧縮したもの

---

### 4.2 4領域 QOL スコアの計算ロジック

FastAPI 側で、以下の計算ルールにより 4つのスコアに集約します。

- 対応付け：
    - **physical** : q1, q2
    - **mental** : q3, q4
    - **social** : q5
    - **environment**: q6, q7
- 計算式：

```
physical    = round( (q1 + q2) / 2 )
mental      = round( (q3 + q4) / 2 )
social      = q5
environment = round( (q6 + q7) / 2 )

```

- `round()` は四捨五入
- 結果は 1〜5 の範囲になるようにクリップ

---

### 4.3 DynamoDB に保存される形式

```json
{
  "user_id": "demo-user",
  "week_key": "2025-W48",
  "qol_scores": {
    "physical": 4,
    "mental": 4,
    "social": 4,
    "environment": 4
  },
  "saved_at": "2025-11-24T09:11:22Z"
}

```

- パーティションキー: `user_id`
- ソートキー: `week_key`

---

## 🧠 5. 推奨行動（Recommendation）の決定ロジック

推奨行動は、**Weekly 指標 + QOL スコア**に基づき、

`Recommendation Engine (engine.py)` によるルールベースで生成されます。

---

### 5.1 入力となる主な指標

- Weekly Metrics（DynamoDB `C2`）
    - `total_steps`
    - `avg_sleep_hours`
    - `evening_walk_minutes`
    - `rhythm_score`
    - `avg_body_battery`（将来）
- QOL Scores（DynamoDB `C1`）
    - `qol_scores.physical`
    - `qol_scores.mental`
    - `qol_scores.social`
    - `qol_scores.environment`

---

### 5.2 内部フラグの例

```python
is_low_steps = total_steps < 25000
is_low_evening_activity = evening_walk_minutes < 30
is_short_sleep = avg_sleep_hours < 6.0
is_irregular_rhythm = rhythm_score < 0.7

low_physical = qol_scores["physical"] <= 3
low_mental   = qol_scores["mental"] <= 3
low_social   = qol_scores["social"] <= 3
low_environment = qol_scores["environment"] <= 3

```

---

### 5.3 ルール例（R1〜R3）

### R1: 歩数が少なく身体QOLも低い → 夕方散歩の推奨

- 条件：
    - `is_low_steps`
    - `low_physical`
- 出力：

```json
{
  "id": "walk_evening",
  "title": "夕方に15分散歩してみましょう",
  "body": "今週は歩数が少なめで、身体に関する自己評価もやや低めでした。夕方に短い散歩を取り入れると、体力づくりと睡眠の質向上につながりやすくなります。",
  "evidence": [
    "今週の総歩数が 25,000 歩未満でした",
    "physical QOL スコアが 3 以下でした"
  ]
}

```

### R2: 睡眠時間が短く、mental が低め → 就寝時間調整の提案

### R3: social が低く、外出時間も少ない → 人との交流を増やす提案

…といった形で R1〜R15 のルール群を拡張していきます。

---

### 5.4 `/recommendations` API レスポンス形式

```json
{
  "recommendations": [
    {
      "id": "walk_evening",
      "title": "夕方に15分散歩してみましょう",
      "body": "今週は歩数が少なめで、身体に関する自己評価もやや低めでした。夕方に短い散歩を取り入れると、体力づくりと睡眠の質向上につながりやすくなります。",
      "evidence": [
        "今週の総歩数が 25,000 歩未満でした",
        "physical QOL スコアが 3 以下でした"
      ]
    }
  ]
}

```

Android 側ではこれを `RecommendationChip`（UiModel）に変換し、

高齢者向けの大きなフォントと十分な余白をもつカードとして表示します。

---

## 📱 6. フロントエンド側の構成（概要）

- `WeeklyScreen`
    - Weekly カード + 推奨カード一覧を描画
- `RecommendationViewModel`
    - `/recommendations` を呼び出し、`UiState` として管理
- `RecommendationCard`
    - 1件の推奨（title, body, evidenceText）をカード表示

---

## 🔚 7. 備考

- Garmin 側で Body Battery データを追加する場合は、
    
    Garmin Developer Portal の設定変更および必要に応じた再審査が必要になる。
    
- システム側は、Body Battery 追加に対応できるよう
    
    ETL / DynamoDB / Recommendation Engine が拡張可能な設計となっている。
