package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TradingSignalEntity
import com.example.ui.TradingViewModel
import com.example.ui.components.VowiiHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ArchivesScreen(viewModel: TradingViewModel) {
    val savedSignals by viewModel.savedSignals.collectAsState()

    val totalCount = savedSignals.size
    val buysCount = savedSignals.count { it.action.equals("BUY", ignoreCase = true) }
    val sellsCount = savedSignals.count { it.action.equals("SELL", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Reusable Top Header (VOWIIai EA | NEURAL KEY: I + Timestamp)
        VowiiHeader(neuralKey = "NEURAL KEY: I")

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Summary Counter Bar (Screenshot 1: TOTAL | BUYS | SELLS)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$totalCount",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "TOTAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }

            // Divider 1
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color(0x22FFFFFF))
            )

            // Buys Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$buysCount",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "BUYS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }

            // Divider 2
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color(0x22FFFFFF))
            )

            // Sells Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$sellsCount",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SELLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        if (savedSignals.isEmpty()) {
            // Screenshot 1 Empty State: Archive Box Icon + NO SIGNALS ARCHIVED YET
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "NO SIGNALS ARCHIVED YET",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "RUN A NEURAL SCAN TO BEGIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else {
            // Signal History Cards List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ARCHIVED SIGNALS (${savedSignals.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { viewModel.reanalyzeAllArchivedSignals() }) {
                        Text("Re-Analyze All", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { viewModel.clearAllArchives() }) {
                        Text("Clear All", fontSize = 11.sp, color = NeonRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedSignals, key = { it.id }) { signal ->
                    ArchivedSignalCard(
                        signal = signal,
                        onDelete = { viewModel.deleteSignal(signal.id) },
                        onReanalyze = { viewModel.reanalyzeArchivedSignal(signal) },
                        onPushToMT5 = {
                            viewModel.pushSignalToMT5(
                                symbol = signal.symbol,
                                action = signal.action,
                                confidence = signal.confidence,
                                sl = signal.stopLoss,
                                tp = signal.takeProfit1
                            )
                            viewModel.selectTab(3)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchivedSignalCard(
    signal: TradingSignalEntity,
    onDelete: () -> Unit,
    onReanalyze: () -> Unit,
    onPushToMT5: () -> Unit
) {
    val isBuy = signal.action.equals("BUY", ignoreCase = true)
    val actionColor = if (isBuy) NeonGreen else NeonRed
    val dateStr = remember(signal.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(signal.timestamp))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0E111A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = actionColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = signal.action,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = actionColor
                        )
                    }
                    if (!signal.chartUri.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "VISION CHART",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Brain Status Line
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x1AFFFFFF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BRAIN STATUS: ${signal.status}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (signal.status.contains("VALIDATED")) NeonGreen else if (signal.status.contains("REVISED")) NeonAmber else TextSecondary
                    )
                    Text(
                        text = "8 Models Synced",
                        fontSize = 8.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Entry Price", fontSize = 9.sp, color = TextMuted)
                    Text("$${signal.entryPrice}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
                }
                Column {
                    Text("Stop Loss", fontSize = 9.sp, color = TextMuted)
                    Text("$${signal.stopLoss}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = NeonRed)
                }
                Column {
                    Text("Take Profit", fontSize = 9.sp, color = TextMuted)
                    Text("$${signal.takeProfit1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = NeonGreen)
                }
                Column {
                    Text("Confidence", fontSize = 9.sp, color = TextMuted)
                    Text("${signal.confidence}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReanalyze,
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("AI RE-ANALYZE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPushToMT5,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CanvasBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PUSH TO MT5", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
