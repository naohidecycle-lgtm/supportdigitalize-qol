package com.sd.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sd.mobile.data.Repository
import com.sd.mobile.data.remote.WeeklyApi
import com.sd.mobile.ui.WeeklyScreen
import com.sd.mobile.ui.WeeklyViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class App: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Moshi 設定（KotlinJsonAdapterFactoryを追加）---
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        // --- Retrofit クライアント構築 ---
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:4010")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val api = retrofit.create(WeeklyApi::class.java)
        val repo = Repository(api)
        val vm = WeeklyViewModel(repo)

        // --- UI 設定 ---
        setContent {
            val state by vm.state.collectAsState()
            LaunchedEffect(Unit) { vm.load() }
            WeeklyScreen(state = state, onRetry = { vm.load() })
        }
    }
}
