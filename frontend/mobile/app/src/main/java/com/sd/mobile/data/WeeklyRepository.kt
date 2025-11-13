package com.sd.mobile.data

import com.sd.mobile.data.remote.AckRequest
import com.sd.mobile.data.remote.AckResponse
import com.sd.mobile.data.remote.WeeklyApi
import com.sd.mobile.data.remote.WeeklyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeeklyRepository(
    private val api: WeeklyApi
) {

    // GET /qol/weekly → List<WeeklyItem> を Result で返す
    suspend fun fetchWeekly(): Result<List<WeeklyItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getWeekly()
            response.items
        }
    }

    // POST /qol/weekly/ack → AckResponse を Result で返す
    suspend fun sendAck(date: String): Result<AckResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.postAck(AckRequest(date = date))
        }
    }
}
