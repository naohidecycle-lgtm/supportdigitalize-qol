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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.mobile.BuildConfig
import com.sd.mobile.data.remote.AckHistoryItem
import com.sd.mobile.data.remote.WeeklyItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeeklyScreen(
    viewModel: WeeklyViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // エラーメッセージが来たら、ユーザー向けに整形してスナックバー表示
    LaunchedEffect(uiState.errorMessage) {
        val raw = uiState.errorMessage
        val userMessage = toUserFriendlySnackbarMessage(raw)

        if (userMessage != null) {
            snackbarHostState.showSnackbar(userMessage)

            // 生のエラー内容はログに残す
            if (!raw.isNullOrBlank()) {
                android.util.Log.e("WeeklyScreen", "raw error: $raw")
            }
        }
    }

    // 画面表示時に ACK 履歴を読み込む
    LaunchedEffect(Unit) {
        viewModel.loadAckHistory()
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
                title = {
                    Column {
                        // 接続先ラベル（TopAppBar の先頭）
                        Text(
                            text = "接続先: ${BuildConfig.BACKEND_ENV_LABEL}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "今週のおすすめ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp
                            )
                        )
                    }
                }
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
        Text(
            text = "読み込み中…",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
        )
    }
}

@Composable
private fun EmptyView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "今週のデータがまだ届いていません。",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "時計やスマートフォンが正しく動いているか確認してから、\n「再読み込み」を押してください。",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(
                text = "再読み込み",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp)
            )
        }
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
            text = "データを読み込めませんでした",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "通信の状態（Wi-Fi / モバイル回線）を確認後、\n「再試行」を押してください。",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(
                text = "再試行",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp)
            )
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "（くわしい情報）",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )
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
        EmptyView(onRetry = onRetry, modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "最近1週間のデータから、\n1日ごとに「おすすめ行動」をまとめています。",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            items(uiState.items) { item ->
                WeeklyCard(
                    item = item,
                    isSendingAck = uiState.isSendingAck,
                    lastAckDate = uiState.lastAckDate,
                    onTryClicked = onTryClicked
                )
            }

            // ↓↓↓ ACK 履歴セクションを追加 ↓↓↓
            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "最近の確認履歴",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                )

                when {
                    uiState.isAckHistoryLoading -> {
                        Text(
                            text = "読み込み中...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    uiState.ackHistoryError != null -> {
                        Text(
                            text = uiState.ackHistoryError
                                ?: "ACK履歴の取得に失敗しました",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    uiState.ackHistory.isEmpty() -> {
                        Text(
                            text = "まだ確認履歴はありません。",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            uiState.ackHistory.forEach { item ->
                                AckHistoryRow(item = item)
                            }
                        }
                    }
                }
            }
            // ↑↑↑ ACK 履歴セクションここまで ↑↑↑
        }
    }
}
@Composable
private fun EvidenceChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun EvidenceChipsSection(
    evidences: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "このおすすめの理由",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            evidences.forEach { evidence ->
                EvidenceChip(text = evidence)
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
    val evidences = remember(item.reason) { buildEvidenceList(item.reason) }
    val chipList =
        if (evidences.isEmpty() && item.reason.isNotBlank()) listOf(item.reason) else evidences

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = formatDateJp(item.date),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "歩数：${item.steps} 歩",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                )
                Text(
                    text = "ストレスの平均：${"%.1f".format(item.stress_avg)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                )
                Text(
                    text = "※ 数が大きいほどストレスが強めです。",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
                )
            }
            Text(
                text = "この日のおすすめ",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = item.recommendation,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
            )
            if (chipList.isNotEmpty()) {
                EvidenceChipsSection(evidences = chipList)
            }
            Button(
                onClick = { onTryClicked(item.date) },
                enabled = !isSendingAck,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isSendingAck) "送信中…" else "やってみる",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp)
                )
            }
            lastAckDate?.let { d ->
                Text(
                    text = "最後に「やってみる」を押した日：$d",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
                )
            }
        }
    }
}
@Composable
private fun AckHistoryRow(
    item: AckHistoryItem
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${item.date} に確認済み",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "端末: ${item.source} / 時刻: ${item.ackAt}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
        )
    }
}

private fun toUserFriendlySnackbarMessage(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val msg = raw.lowercase()
    return when {
        msg.contains("failed to connect") ||
                msg.contains("timeout") ||
                msg.contains("unable to resolve host") ->
            "通信エラーが発生しました。ネットワークをご確認ください。"

        else ->
            "エラーが発生しました。しばらくしてからお試しください。"
    }
}

private fun formatDateJp(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.JAPAN)
        val youbi = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "月"
            DayOfWeek.TUESDAY -> "火"
            DayOfWeek.WEDNESDAY -> "水"
            DayOfWeek.THURSDAY -> "木"
            DayOfWeek.FRIDAY -> "金"
            DayOfWeek.SATURDAY -> "土"
            DayOfWeek.SUNDAY -> "日"
        }
        "${date.format(formatter)}（$youbi）"
    } catch (e: Exception) {
        dateString
    }
}

private fun buildEvidenceList(reason: String): List<String> {
    return reason
        .replace("。", "、")
        .split("、", "・", ";", "，")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
