package com.sd.mobile

import android.app.Application
import com.sd.mobile.data.WeeklyRepository

class App : Application() {

    lateinit var repository: WeeklyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = WeeklyRepository()   // ★ 引数なしでOK
    }
}
