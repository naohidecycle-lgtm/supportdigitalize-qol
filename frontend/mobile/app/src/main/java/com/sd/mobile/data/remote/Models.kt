package com.sd.mobile.data.remote

data class WeeklyItem(
    val date: String,
    val steps: Int,
    val stress_avg: Double,
    val recommendation: String?,
    val reason: String?
)

data class WeeklyResponse(
    val items: List<WeeklyItem>
)
