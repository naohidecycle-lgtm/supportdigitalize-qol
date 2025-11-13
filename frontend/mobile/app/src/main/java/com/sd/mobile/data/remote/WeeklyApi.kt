package com.sd.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WeeklyApi {

    // GET /qol/weekly
    @GET("qol/weekly")
    suspend fun getWeekly(): WeeklyListResponse

    // POST /qol/weekly/ack
    @POST("qol/weekly/ack")
    suspend fun postAck(
        @Body body: AckRequest
    ): AckResponse
}
