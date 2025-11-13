package com.sd.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sd.mobile.data.remote.WeeklyItem
import androidx.compose.material3.ExperimentalMaterial3Api


@Composable
fun WeeklyScreen(
    viewModel: WeeklyViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // エラーメッセージが来たらスナックバーで表示
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    WeeklyScreenContent(
        uiState = uiState,
        onRetry = { viewModel.loadWeekly() },
        onTryClicked = { date -> viewModel.sendAck(date) },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyScreenContent(
    uiState: WeeklyUiState,
    onRetry: () -> Unit,
    onTryClicked: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今週のおすすめ") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                LoadingView(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                )
            }

            uiState.errorMessage != null && uiState.items.isEmpty() -> {
                ErrorView(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                )
            }

            else -> {
                SuccessView(
                    uiState = uiState,
                    onRetry = onRetry,
                    onTryClicked = onTryClicked,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("読み込み中…")
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "データの取得に失敗しました",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(text = message)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("再試行")
        }
    }
}

@Composable
private fun SuccessView(
    uiState: WeeklyUiState,
    onRetry: () -> Unit,
    onTryClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.items.isEmpty()) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("今週のデータがありません")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("再読み込み")
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.items) { item ->
                WeeklyCard(
                    item = item,
                    isSendingAck = uiState.isSendingAck,
                    lastAckDate = uiState.lastAckDate,
                    onTryClicked = onTryClicked
                )
            }
        }
    }
}

@Composable
private fun WeeklyCard(
    item: WeeklyItem,
    isSendingAck: Boolean,
    lastAckDate: String?,
    onTryClicked: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "日付: ${item.date}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("歩数: ${item.steps}")
            Text("ストレス平均: ${"%.1f".format(item.stressAvg)}")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "おすすめ行動",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(item.recommendation)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "理由",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(item.reason)
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { onTryClicked(item.date) },
                enabled = !isSendingAck
            ) {
                Text(if (isSendingAck) "送信中…" else "やってみる")
            }

            lastAckDate?.let { d ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "最後に「やってみる」を押した日: $d",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
