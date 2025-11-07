package com.sd.mobile.data

import com.sd.mobile.data.remote.WeeklyApi
import com.sd.mobile.data.remote.WeeklyResponse

class Repository(private val api: WeeklyApi) {
    suspend fun fetchWeekly(): WeeklyResponse = api.getWeekly()
}
