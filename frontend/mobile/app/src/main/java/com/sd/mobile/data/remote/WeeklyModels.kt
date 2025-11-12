package com.sd.mobile.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeeklyListResponse(
    @Json(name = "items") val items: List<WeeklyItem>
)

@JsonClass(generateAdapter = true)
data class WeeklyItem(
    val date: String,
    val steps: Int,
    @Json(name = "stress_avg") val stressAvg: Double,
    val sleep_hours: Double?,
    val recommendation: String,
    val reason: String
)
