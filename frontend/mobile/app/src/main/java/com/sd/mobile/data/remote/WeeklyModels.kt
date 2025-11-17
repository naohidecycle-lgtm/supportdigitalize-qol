package com.sd.mobile.data.remote

// 1日分のデータ
data class WeeklyItem(
    val date: String,
    val steps: Int,
    val stress_avg: Double,
    val recommendation: String,
    val reason: String
)

// GET /qol/weekly のレスポンス
data class WeeklyResponse(
    val items: List<WeeklyItem>
)

// POST /qol/weekly/ack のリクエストボディ
data class AckRequest(
    val date: String
)

// POST /qol/weekly/ack のレスポンス
data class AckResponse(
    val ok: Boolean,
    val receivedDate: String
)
