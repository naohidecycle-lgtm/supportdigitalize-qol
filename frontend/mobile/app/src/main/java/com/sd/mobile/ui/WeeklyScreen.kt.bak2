package com.sd.mobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WeeklyScreenRoot() {
    val vm: WeeklyViewModel = viewModel() // Factory注入している場合は引数で渡す
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }
    WeeklyScreen(state = state, onRetry = { vm.load() })
}

@Composable
fun WeeklyScreen(
    state: UiState,
    onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> LoadingView()
        is UiState.Success -> SuccessView(state)
        is UiState.Error   -> ErrorView(state.type, onRetry)
    }
}

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.padding(24.dp))
    }
}

@Composable
private fun SuccessView(s: UiState.Success) {
    Column(Modifier.padding(16.dp)) {
        // 推奨カード（例）
        Text(
            text = s.item.recommendation ?: "今週の推奨はありません",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = s.item.reason ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        // Evidence Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("歩数: ${s.steps}") })
            AssistChip(onClick = {}, label = { Text("ストレス: ${s.stressAvg}") })
            AssistChip(onClick = {}, label = { Text("睡眠: ${s.sleepHours}") })
        }
    }
}

@Composable
private fun ErrorView(type: ErrorType, onRetry: () -> Unit) {
    val msg = when (type) {
        ErrorType.NETWORK -> "ネットワークに接続できません。再試行してください。"
        ErrorType.SERVER  -> "サーバでエラーが発生しました。しばらくして再試行してください。"
        ErrorType.PARSE   -> "データの読み取りに失敗しました。"
        ErrorType.EMPTY   -> "データが見つかりません。"
        ErrorType.UNKNOWN -> "不明なエラーが発生しました。"
    }
    Column(Modifier.padding(16.dp)) {
        Text(text = msg, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("再試行") }
    }
}
