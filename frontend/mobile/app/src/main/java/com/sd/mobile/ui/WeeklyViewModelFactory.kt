package com.sd.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sd.mobile.data.WeeklyRepository

class WeeklyViewModelFactory(
    private val repository: WeeklyRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeeklyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeeklyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
