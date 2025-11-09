package com.sd.mobile.data

import com.sd.mobile.data.remote.WeeklyApi
import com.sd.mobile.data.remote.WeeklyResponse
import com.squareup.moshi.JsonDataException
import java.io.IOException
import retrofit2.HttpException

class Repository(private val api: WeeklyApi) {

    // 簡易ドメインエラー定義（このファイル内で完結）
    sealed interface DomainError {
        data object NETWORK : DomainError
        data class CLIENT(val code: Int) : DomainError
        data class SERVER(val code: Int) : DomainError
        data object PARSE : DomainError
        data object UNKNOWN : DomainError
    }

    class DomainException(
        val error: DomainError,
        cause: Throwable? = null
    ) : Exception(cause)

    // ==== fetchWeekly(): エラーをDomainErrorへ変換 ====
    suspend fun fetchWeekly(): WeeklyResponse {
        try {
            return api.getWeekly()
        } catch (e: IOException) {
            throw DomainException(DomainError.NETWORK, e)
        } catch (e: HttpException) {
            val code = e.code()
            val domain = when (code) {
                in 400..499 -> DomainError.CLIENT(code)
                in 500..599 -> DomainError.SERVER(code)
                else -> DomainError.UNKNOWN
            }
            throw DomainException(domain, e)
        } catch (e: JsonDataException) {
            throw DomainException(DomainError.PARSE, e)
        } catch (e: Exception) {
            throw DomainException(DomainError.UNKNOWN, e)
        }
    }
}
