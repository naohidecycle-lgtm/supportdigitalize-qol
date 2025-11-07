package com.sd.mobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WeeklyScreen(state: UiState, onRetry: ()->Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when(state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                Text("読み込みに失敗しました: ${state.message}")
                Spacer(Modifier.height(8.dp))
                Button(onClick = onRetry) { Text("再試行") }
            }
            is UiState.Success -> Card(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("本日の推奨", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(state.item.recommendation ?: "（推奨なし）", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("理由", fontWeight = FontWeight.SemiBold)
                    Text(state.item.reason ?: "（理由データなし）", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { }) { Text("やってみる") }
                        TextButton(onClick = { }) { Text("後で") }
                    }
                }
            }
        }
    }
}
