package com.sd.mobile

import android.app.Application
import com.sd.mobile.data.Repository
import com.sd.mobile.data.remote.WeeklyApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class App : Application() {

    // エミュレータ → ホストPCの localhost
    private val BASE_URL = "http://10.0.2.2:4010"

    // Debug のときだけHTTPログを出す（任意）
    private val okHttp by lazy {
        val builder = OkHttpClient.Builder()
        // BuildConfig.DEBUG が使えるなら以下を有効化
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(logging)
        builder.build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val api: WeeklyApi by lazy {
        retrofit.create(WeeklyApi::class.java)
    }

    // ここで Repository に WeeklyApi を注入
    val repository: Repository by lazy {
        Repository(api)
    }
}
