package com.sd.mobile.data

import com.sd.mobile.data.remote.AckHistoryItem
import com.sd.mobile.data.remote.AckRequest
import com.sd.mobile.data.remote.AckResponse
import com.sd.mobile.data.remote.WeeklyApi
import com.sd.mobile.data.remote.WeeklyResponse


/**
 * M5 用のシンプルな Repository 実装
 * - WeeklyApi.getWeekly() / WeeklyApi.postAck() をそのまま委譲
 * - 戻り値は Result<> でラップ
 */
class WeeklyRepository {

    suspend fun getWeekly(): Result<WeeklyResponse> =
        WeeklyApi.getWeekly()

    suspend fun postAck(request: AckRequest): Result<AckResponse> =
        WeeklyApi.postAck(request)

    suspend fun getAckHistory(limit: Int = 10): Result<List<AckHistoryItem>> {
        return WeeklyApi.getAckHistory(limit).mapCatching { response ->
            response.items
        }
    }
}



