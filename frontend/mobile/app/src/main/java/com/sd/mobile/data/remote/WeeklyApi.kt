package com.sd.mobile.data.remote

import com.sd.mobile.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WeeklyApiService {

    @GET("qol/weekly")
    suspend fun getWeekly(): WeeklyResponse

    @POST("qol/weekly/ack")
    suspend fun postAck(
        @Body body: AckRequest
    ): AckResponse

    @GET("qol/weekly/ack/history")
    suspend fun getAckHistory(
        @Query("limit") limit: Int = 10
    ): AckHistoryResponse
}

object WeeklyApi {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val service: WeeklyApiService by lazy {
        retrofit.create(WeeklyApiService::class.java)
    }

    suspend fun getWeekly(): Result<WeeklyResponse> =
        runCatching { service.getWeekly() }

    suspend fun postAck(request: AckRequest): Result<AckResponse> =
        runCatching { service.postAck(request) }

    suspend fun getAckHistory(limit: Int = 10): Result<AckHistoryResponse> =
        runCatching { service.getAckHistory(limit) }
}
