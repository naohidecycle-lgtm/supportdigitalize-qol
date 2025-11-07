package com.sd.mobile.data.remote

import retrofit2.http.GET

interface WeeklyApi {
    @GET("/qol/weekly")
    suspend fun getWeekly(): WeeklyResponse
}
