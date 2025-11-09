package com.sd.mobile.data.remote

data class WeeklyResponse(
    val items: List<WeeklyItem>
)

data class WeeklyItem(
    val date: String,
    val steps: Int?,
    val stress_avg: Double?,
    val sleep_hours: Double?,           // ★ これを追加
    val recommendation: String?,        // モックに合わせて保持（任意）
    val reason: String?                 // 同上
)
