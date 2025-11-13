package com.sd.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sd.mobile.data.WeeklyRepository
import com.sd.mobile.data.remote.WeeklyItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeeklyUiState(
    val isLoading: Boolean = false,
    val items: List<WeeklyItem> = emptyList(),
    val errorMessage: String? = null,
    val isSendingAck: Boolean = false,
    val lastAckDate: String? = null
)

class WeeklyViewModel(
    private val repository: WeeklyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WeeklyUiState(isLoading = true)
    )
    val uiState: StateFlow<WeeklyUiState> = _uiState.asStateFlow()

    init {
        loadWeekly()
    }

    fun loadWeekly() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.fetchWeekly()
            result
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = list,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "データの取得に失敗しました"
                        )
                    }
                }
        }
    }

    fun sendAck(date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingAck = true, errorMessage = null) }

            val result = repository.sendAck(date)
            result
                .onSuccess { ack ->
                    _uiState.update {
                        it.copy(
                            isSendingAck = false,
                            lastAckDate = ack.receivedDate
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSendingAck = false,
                            errorMessage = e.message ?: "送信に失敗しました"
                        )
                    }
                }
        }
    }
}
