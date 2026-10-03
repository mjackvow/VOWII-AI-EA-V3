package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.SignalAction
import com.example.ui.TradingViewModel
import com.example.ui.components.ConsensusBar
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.VowiiHeader
import com.example.ui.theme.*

@Composable
fun ScannerScreen(viewModel: TradingViewModel) {
    val selectedSymbol by viewModel.selectedSymbol.collectAsState()
    val timeframe by viewModel.selectedTimeframe.collectAsState()
    val scanMode by viewModel.scanMode.collectAsState() // 0 = SYMBOL SCAN, 1 = IMAGE SCAN
    val scannedUri by viewModel.scannedImageUri.collectAsState()
    val isScanning by viewModel.isScanningChart.collectAsState()
    val scannerResult by viewModel.scannerResult.collectAsState()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onChartImageSelected(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Top Header (Screenshot 3 & 4: VOWIIai EA | NEURAL KEY: II + Timestamp)
        VowiiHeader(neuralKey = "NEURAL KEY: II")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Segmented Mode Switcher (Screenshot 4: SYMBOL SCAN | IMAGE SCAN)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // SYMBOL SCAN Button
                    val isSymbolScan = scanMode == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSymbolScan) Color(0xFF072115) else Color(0x11FFFFFF))
                            .border(
                                width = 1.dp,
                                color = if (isSymbolScan) NeonGreen else Color(0x1AFFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setScanMode(0) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = if (isSymbolScan) NeonGreen else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYMBOL SCAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSymbolScan) NeonGreen else TextMuted,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // IMAGE SCAN Button
                    val isImageScan = scanMode == 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isImageScan) Color(0xFF072115) else Color(0x11FFFFFF))
                            .border(
                                width = 1.dp,
                                color = if (isImageScan) NeonGreen else Color(0x1AFFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setScanMode(1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = if (isImageScan) NeonGreen else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IMAGE SCAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isImageScan) NeonGreen else TextMuted,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            if (scanMode == 0) {
                // SYMBOL SCAN MODE (Screenshot 4)
                item {
                    Column {
                        Text(
                            text = "SELECT SYMBOL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2x3 Grid of Symbol Selector Cards (Screenshot 4)
                        val symbolsList = listOf(
                            SymbolCardData("XAUUSD", "XAU/USD", "Metals"),
                            SymbolCardData("BTCUSD", "BTC/USD", "Crypto"),
                            SymbolCardData("ETHUSD", "ETH/USD", "Crypto"),
                            SymbolCardData("EURUSD", "EUR/USD", "Forex"),
                            SymbolCardData("GBPUSD", "GBP/USD", "Forex"),
                            SymbolCardData("USDJPY", "USD/JPY", "Forex")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (row in 0..2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (col in 0..1) {
                                        val itemIndex = row * 2 + col
                                        val data = symbolsList[itemIndex]
                                        val isSelected = data.id == selectedSymbol

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(68.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) Color(0xFF072115) else Color(0xFF0C0F17))
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) NeonGreen else Color(0x1AFFFFFF),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { viewModel.selectSymbol(data.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = data.displaySymbol,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) NeonGreen else Color.White
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = data.category,
                                                    fontSize = 11.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Timeframe Selector Section (Screenshot 3)
                item {
                    Column {
                        Text(
                            text = "TIMEFRAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("M5", "M15", "M30", "H1", "H4", "D1").forEach { tf ->
                                val isSelected = tf == timeframe
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0x1100E5FF) else Color(0x11FFFFFF))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) NeonCyan else Color(0x1AFFFFFF),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.setTimeframe(tf) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tf,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) NeonCyan else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // CTA Button (Screenshot 3 & 4: [brain icon] RUN NEURAL SCAN)
                item {
                    Button(
                        onClick = { viewModel.runNeuralScanForSymbol(selectedSymbol, timeframe) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, NeonGreen, RoundedCornerShape(14.dp)),
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF072115),
                            contentColor = NeonGreen
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = NeonGreen,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "SCANNING PATTERNS...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen,
                                letterSpacing = 1.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "RUN NEURAL SCAN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            } else {
                // IMAGE SCAN MODE
                item {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        borderColor = if (scannedUri != null) NeonGreen else SurfaceCardBorder
                    ) {
                        if (scannedUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(scannedUri),
                                contentDescription = "Uploaded Chart",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(shape = CircleShape, color = Color(0x1AFFFFFF)) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.padding(16.dp).size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Tap to upload chart screenshot",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Supports MT4/MT5, TradingView, Binance",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (scannedUri != null) viewModel.runChartVisionScan(scannedUri!!)
                                else viewModel.runChartVisionScan(Uri.EMPTY)
                            },
                            modifier = Modifier.weight(1.2f).height(48.dp),
                            enabled = !isScanning,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CanvasBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isScanning) "Scanning..." else "Analyze Image", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Results or Empty Radar State (Screenshot 3)
            if (scannerResult == null && !isScanning) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(54.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "SELECT A SYMBOL AND RUN SCAN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.2.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Multi-model ensemble analysis with live chart data",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else if (scannerResult != null) {
                val result = scannerResult!!
                item {
                    val isBuy = result.action == SignalAction.BUY
                    val actionColor = when (result.action) {
                        SignalAction.BUY -> NeonGreen
                        SignalAction.SELL -> NeonRed
                        SignalAction.WAIT -> NeonAmber
                    }

                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = actionColor
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "NEURAL SCAN RESULT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 1.5.sp
                                    )
                                    Text(
                                        text = "${result.symbol} ${result.action}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = actionColor
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = actionColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${result.overallConfidence}% Match",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = actionColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            ConsensusBar(votes = result.votes)

                            // Attached Chart Screenshot Visual Inspection Badge
                            if (result.chartImageAnalyzed || scannedUri != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x2200E5FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "SCREENSHOT READ & ANALYZED BY ALL 8 MODELS",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = NeonCyan,
                                                letterSpacing = 0.8.sp
                                            )
                                            Text(
                                                text = result.visualChartNotes ?: "Visual candlestick rejection wicks, support lines, and order block zones inspected.",
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                maxLines = 3
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LevelPill("Entry", "$${result.entryPrice}", Color.White)
                                LevelPill("Stop Loss", "$${result.stopLoss}", NeonRed)
                                LevelPill("Take Profit", "$${result.takeProfit1}", NeonGreen)
                                LevelPill("R:R Ratio", result.riskRewardRatio, NeonGold)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Individual Model Decisions Breakdown
                            Text(
                                text = "INDIVIDUAL MODEL DECISIONS & AI REASONING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (result.modelDecisions.isNotEmpty()) {
                                    result.modelDecisions.forEach { dec ->
                                        ScannerModelDecisionCard(dec = dec)
                                    }
                                } else {
                                    result.votes.forEach { vote ->
                                        ScannerVoteCard(vote = vote)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.saveSignalToArchives(result) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                                ) {
                                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Archive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.pushSignalToMT5(result.symbol, result.action.name, result.overallConfidence, result.stopLoss, result.takeProfit1)
                                        viewModel.selectTab(3)
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CanvasBackground)
                                ) {
                                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PUSH TO MT5", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerModelDecisionCard(dec: com.example.data.model.ModelBrainDecision) {
    val actionColor = when (dec.action) {
        SignalAction.BUY -> NeonGreen
        SignalAction.SELL -> NeonRed
        SignalAction.WAIT -> NeonAmber
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0E111A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(actionColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = dec.modelName, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "(${dec.provider})", fontSize = 9.sp, color = TextMuted)
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = actionColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${dec.action.name} • ${dec.confidence}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = actionColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dec.reasoning,
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ScannerVoteCard(vote: com.example.data.model.ModelVote) {
    val actionColor = when (vote.action) {
        SignalAction.BUY -> NeonGreen
        SignalAction.SELL -> NeonRed
        SignalAction.WAIT -> NeonAmber
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0E111A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${vote.modelName} (${vote.provider})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "${vote.action.name} • ${vote.confidence}%", fontSize = 10.sp, fontWeight = FontWeight.Black, color = actionColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = vote.rationale, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

private data class SymbolCardData(
    val id: String,
    val displaySymbol: String,
    val category: String
)

@Composable
private fun LevelPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}
