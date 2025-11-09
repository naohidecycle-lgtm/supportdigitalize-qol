package com.sd.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sd.mobile.data.Repository
import com.sd.mobile.data.remote.WeeklyItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

sealed interface UiState {
    object Loading : UiState
    data class Success(
        val item: WeeklyItem,
        val steps: String,
        val stressAvg: String,
        val sleepHours: String
    ) : UiState
    data class Error(val type: ErrorType) : UiState
}

enum class ErrorType { NETWORK, SERVER, PARSE, EMPTY, UNKNOWN }

class WeeklyViewModel(private val repo: Repository) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state

    // UI側でLaunchedEffectから呼ぶ運用なら、このinitは削除してOK
    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val res = repo.fetchWeekly()
                val first = res.items.firstOrNull()
                if (first != null) {
                    val steps = formatSteps(first.steps)
                    val stressAvg = formatDouble(first.stress_avg)
                    val sleepHours = formatDouble(first.sleep_hours, suffix = " h")
                    _state.value = UiState.Success(first, steps, stressAvg, sleepHours)
                } else {
                    _state.value = UiState.Error(ErrorType.EMPTY)
                }
            } catch (e: Repository.DomainException) {
                val type = when (e.error) {
                    is Repository.DomainError.NETWORK -> ErrorType.NETWORK
                    is Repository.DomainError.CLIENT  -> ErrorType.SERVER   // 必要なら CLIENT を別型に分離可
                    is Repository.DomainError.SERVER  -> ErrorType.SERVER
                    is Repository.DomainError.PARSE   -> ErrorType.PARSE
                    is Repository.DomainError.UNKNOWN -> ErrorType.UNKNOWN
                }
                _state.value = UiState.Error(type)
            } catch (e: Exception) {
                _state.value = UiState.Error(ErrorType.UNKNOWN)
            }
        }
    }

    private fun formatSteps(steps: Int?): String =
        steps?.let { NumberFormat.getNumberInstance(Locale.US).format(it) } ?: "—"

    private fun formatDouble(value: Double?, suffix: String = ""): String =
        value?.let { String.format(Locale.US, "%.1f%s", it, suffix) } ?: "—"
}
