package com.sd.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sd.mobile.data.Repository
import com.sd.mobile.data.remote.WeeklyItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    object Loading: UiState
    data class Success(val item: WeeklyItem): UiState
    data class Error(val message: String): UiState
}

class WeeklyViewModel(private val repo: Repository): ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            try {
                val res = repo.fetchWeekly()
                val first = res.items.firstOrNull()
                if (first != null) _state.value = UiState.Success(first)
                else _state.value = UiState.Error("No data")
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
