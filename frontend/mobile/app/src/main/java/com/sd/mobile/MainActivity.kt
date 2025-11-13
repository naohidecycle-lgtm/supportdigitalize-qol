package com.sd.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.sd.mobile.ui.WeeklyScreen
import com.sd.mobile.ui.WeeklyViewModel
import com.sd.mobile.ui.WeeklyViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: WeeklyViewModel by viewModels {
        val app = application as App
        WeeklyViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeeklyScreen(viewModel = viewModel)
        }
    }
}
