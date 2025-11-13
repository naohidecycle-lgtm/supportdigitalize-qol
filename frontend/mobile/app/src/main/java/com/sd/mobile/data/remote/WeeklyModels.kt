package com.sd.mobile.data.remote

import com.google.gson.annotations.SerializedName

// /qol/weekly の1件分
data class WeeklyItem(
    @SerializedName("date") val date: String,
    @SerializedName("steps") val steps: Int,
    // JSON は "stress_avg" だが、UI では stressAvg で扱いたいのでマッピング
    @SerializedName("stress_avg") val stressAvg: Double,
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("reason") val reason: String
)

// /qol/weekly のレスポンス全体
data class WeeklyListResponse(
    @SerializedName("items") val items: List<WeeklyItem>
)

// POST /qol/weekly/ack のリクエストボディ
data class AckRequest(
    @SerializedName("date") val date: String
)

// POST /qol/weekly/ack のレスポンス
data class AckResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("receivedDate") val receivedDate: String
)
