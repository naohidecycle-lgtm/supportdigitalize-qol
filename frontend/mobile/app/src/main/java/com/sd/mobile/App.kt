package com.sd.mobile

import android.app.Application
import com.sd.mobile.data.WeeklyRepository
import com.sd.mobile.data.remote.WeeklyApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    companion object {
        // エミュレータ → ホストPCの localhost
        private const val BASE_URL = "http://10.0.2.2:4010/"
    }

    // HTTPクライアント（ログ付き）
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Retrofit 本体（Gson コンバータ付き）
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API クライアント
    val api: WeeklyApi by lazy {
        retrofit.create(WeeklyApi::class.java)
    }

    // Repository（ViewModel から使う窓口）
    val repository: WeeklyRepository by lazy {
        WeeklyRepository(api)
    }
}
