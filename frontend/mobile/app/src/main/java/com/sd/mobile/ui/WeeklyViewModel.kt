package com.sd.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sd.mobile.data.WeeklyRepository
import com.sd.mobile.data.remote.AckHistoryItem
import com.sd.mobile.data.remote.AckRequest
import com.sd.mobile.data.remote.WeeklyItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeeklyUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<WeeklyItem> = emptyList(),

    // ACK履歴用
    val ackHistory: List<AckHistoryItem> = emptyList(),
    val isAckHistoryLoading: Boolean = false,
    val ackHistoryError: String? = null,

    // ACK送信状態用（M5で既に使っていたもの）
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
        // ACK履歴も画面表示時に読み込む場合は、これを有効にする
        // loadAckHistory()
    }

    fun loadWeekly() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            repository.getWeekly()
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = response.items,
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

    fun loadAckHistory(limit: Int = 10) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isAckHistoryLoading = true,
                    ackHistoryError = null
                )
            }

            val result = repository.getAckHistory(limit)

            result
                .onSuccess { items ->
                    _uiState.update { state ->
                        state.copy(
                            isAckHistoryLoading = false,
                            ackHistory = items
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isAckHistoryLoading = false,
                            ackHistoryError = throwable.message
                                ?: "ACK履歴の取得に失敗しました"
                        )
                    }
                }
        }
    }

    /** M5: Repository.postAck を呼ぶ */
    fun sendAck(date: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingAck = true,
                    errorMessage = null
                )
            }

            repository.postAck(
                AckRequest(
                    date = date
                )
            )
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
                            errorMessage = e.message ?: "ACK送信に失敗しました"
                        )
                    }
                }
        }
    }
}
