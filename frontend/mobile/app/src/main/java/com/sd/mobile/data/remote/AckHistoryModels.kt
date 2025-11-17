package com.sd.mobile.data.remote

import com.google.gson.annotations.SerializedName

data class AckHistoryItem(
    @SerializedName("date")
    val date: String,          // 例: "2025-11-16"

    @SerializedName("source")
    val source: String,        // 例: "android-release"

    @SerializedName("user_id")
    val userId: String,        // 例: "demo-user"

    @SerializedName("requestId")
    val requestId: String,     // 例: "UIP_ehXHNjMEJew="

    @SerializedName("ackAt")
    val ackAt: String          // 例: "2025-11-16T09:04:22.535367+00:00"
)

data class AckHistoryResponse(
    @SerializedName("items")
    val items: List<AckHistoryItem> = emptyList()
)
