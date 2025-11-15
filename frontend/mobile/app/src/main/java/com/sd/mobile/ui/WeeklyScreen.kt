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
import com.sd.mobile.data.remote.WeeklyItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.util.Locale
import com.sd.mobile.BuildConfig

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

            // 生のエラー内容はログに残す（ユーザーには見せない）
            if (!raw.isNullOrBlank()) {
                android.util.Log.e("WeeklyScreen", "raw error: $raw")
            }
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
                title = {
                    Text(
                        text = "今週のおすすめ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp
                        )
                    )
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
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp
            )
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
        // メインメッセージ
        Text(
            text = "今週のデータがまだ届いていません。",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
        )

        Spacer(Modifier.height(8.dp))

        // 次にどうすればよいか
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
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 18.sp
                )
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
        // タイトル：何が起きたか
        Text(
            text = "データを読み込めませんでした",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(8.dp))

        // 説明：どうすればよいか（次の一手）
        Text(
            text = "通信の状態（Wi-Fi やモバイル回線）を確認してから、\n「再試行」を押してください。",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(
                text = "再試行",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 18.sp
                )
            )
        }

        // くわしい情報（開発者・テスター向け）※小さめ表示
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
        // データが 0 件のとき
        EmptyView(
            onRetry = onRetry,
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 画面の説明文（最初に 1 回だけ表示）
            item {
                Text(
                    text = "最近1週間のデータから、1日ごとに「あなたへのおすすめ行動」をまとめています。",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            // 1 日分ずつカード表示（ここが消えないように注意）
            items(uiState.items) { item ->
                WeeklyCard(
                    item = item,
                    isSendingAck = uiState.isSendingAck,
                    lastAckDate = uiState.lastAckDate,
                    onTryClicked = onTryClicked
                )
            }

            // ▼ ここから追加：リストの一番下に接続先ラベルを出す
            item {
                if (BuildConfig.DEBUG) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "接続先: Prism (debug)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
            // ▲ ここまで追加
        }
    }
}



// インクルーシブ対応済み：縦並び・幅広・行間広め
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
        modifier = modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp)) // ← 任意の追加
        Text(
            text = "このおすすめの理由",
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 16.sp
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
    // reason から Evidence のリストを生成
    val evidences = remember(item.reason) { buildEvidenceList(item.reason) }
    val chipList =
        if (evidences.isEmpty() && item.reason.isNotBlank()) listOf(item.reason) else evidences

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // カード内余白（インクルーシブ指針）
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 日付タイトル（22sp）→ 日本語形式＋曜日
            Text(
                text = formatDateJp(item.date),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )


            // 基本指標：行を分けて読みやすく
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "歩数：${item.steps} 歩",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                )
                Text(
                    text = "ストレスの平均：${
                        "%.1f".format(
                            item.stress_avg
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                )
                Text(
                    text = "※ 数が大きいほどストレスが強めです。",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                )
            }


            // おすすめ行動ラベル
            Text(
                text = "この日のおすすめ",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // おすすめ行動の本文
            Text(
                text = item.recommendation,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
            )

            // おすすめの根拠（EvidenceChip 群）
            if (chipList.isNotEmpty()) {
                EvidenceChipsSection(evidences = chipList)
            }

            // 「やってみる」ボタン（ACK送信）
            Button(
                onClick = { onTryClicked(item.date) },
                enabled = !isSendingAck,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp) // 高齢者向けに十分なタップ領域
            ) {
                Text(
                    text = if (isSendingAck) "送信中…" else "やってみる",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 18.sp
                    )
                )
            }

            // 最後に押した日
            lastAckDate?.let { d ->
                Text(
                    text = "最後に「やってみる」を押した日：$d",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}


// Snackbar 用：生のエラー文字列を、高齢者向けの日本語メッセージに変換
private fun toUserFriendlySnackbarMessage(raw: String?): String? {
    if (raw.isNullOrBlank()) return null

    val msg = raw.lowercase()

    return when {
        // ネットワーク接続系エラー
        msg.contains("failed to connect") ||
                msg.contains("timeout") ||
                msg.contains("unable to resolve host") -> {
            "通信エラーが発生しました。ネットワークの状態をご確認ください。"
        }

        // それ以外は汎用メッセージ
        else -> {
            "エラーが発生しました。しばらくしてからもう一度お試しください。"
        }
    }
}


// yyyy-MM-dd → 2019年8月24日（土） 形式に変換
private fun formatDateJp(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString) // yyyy-MM-dd をパース
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
        dateString // パースできないときは元の文字列を返す
    }
}

private fun buildEvidenceList(reason: String): List<String> {
    // 「。」を「、」にそろえてから、いくつかの区切り文字で split
    return reason
        .replace("。", "、")
        .split("、", "・", ";", "，")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

