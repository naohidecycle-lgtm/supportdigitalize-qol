package com.sd.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sd.mobile.data.WeeklyRepository
import com.sd.mobile.data.remote.WeeklyItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface WeeklyUiState {
    data object Loading : WeeklyUiState
    data class Success(val items: List<WeeklyItem>) : WeeklyUiState
    data class Error(val message: String) : WeeklyUiState
}

sealed interface WeeklyUiEvent {
    data class ShowMessage(val message: String) : WeeklyUiEvent
}

class WeeklyViewModel(
    private val repository: WeeklyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeeklyUiState>(WeeklyUiState.Loading)
    val uiState: StateFlow<WeeklyUiState> = _uiState

    private val _events = Channel<WeeklyUiEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        reload()
    }

    fun reload() {
        _uiState.value = WeeklyUiState.Loading
        viewModelScope.launch {
            repository.fetchWeekly()
                .onSuccess { items -> _uiState.value = WeeklyUiState.Success(items) }
                .onFailure { t -> _uiState.value = WeeklyUiState.Error(userFriendlyMessage(t)) }
        }
    }

    fun onTryClicked() {
        viewModelScope.launch {
            _events.send(WeeklyUiEvent.ShowMessage("実行しました"))
        }
    }

    private fun userFriendlyMessage(t: Throwable): String {
        val raw = t.message?.ifBlank { null }
        return "通信に失敗しました。Prismの起動やネットワークをご確認ください" +
                (raw?.let { "\n\n詳細: $it" } ?: "")
    }
}

