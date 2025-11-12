package com.sd.mobile.data

import com.sd.mobile.data.remote.WeeklyApi
import com.sd.mobile.data.remote.WeeklyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeeklyRepository(private val api: WeeklyApi) {

    /** GET /qol/weekly を取得。成功なら items を返す。 */
    suspend fun fetchWeekly(): Result<List<WeeklyItem>> = withContext(Dispatchers.IO) {
        runCatching { api.getWeekly().items }
    }

    /** まだ未使用。POST/ACK を後続で実装予定。 */
    suspend fun postAckStub(): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }
}
