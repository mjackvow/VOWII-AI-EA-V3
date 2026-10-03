package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModelBrainDecision
import com.example.data.model.SignalAction
import com.example.data.model.SymbolQuote
import com.example.ui.TradingViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.VowiiHeader
import com.example.ui.theme.*

@Composable
fun LiveSignalsScreen(viewModel: TradingViewModel) {
    val activeDetailSymbol by viewModel.activeDetailSymbol.collectAsState()

    // If a symbol detail is open, show the full Chart & Analysis detail screen
    if (activeDetailSymbol != null) {
        SymbolDetailScreen(
            symbol = activeDetailSymbol!!,
            viewModel = viewModel,
            onBack = { viewModel.closeSymbolDetail() }
        )
        return
    }

    val quotesMap by viewModel.symbolQuotes.collectAsState()
    val initialQuotes = remember { viewModel.getInitialSymbols() }
    val symbolList = if (quotesMap.isNotEmpty()) quotesMap.values.toList() else initialQuotes

    val analysisSession by viewModel.modelsAnalysisSession.collectAsState()
    val focusMarket by viewModel.modelsFocusMarket.collectAsState()
    val availableMarkets = viewModel.availableMarkets
    val newsList by viewModel.liveNewsList.collectAsState()

    val timerSeconds by viewModel.brainTimerSeconds.collectAsState()
    val brainProcessStatus by viewModel.brainProcessStatus.collectAsState()
    val priceAudits by viewModel.priceAudits.collectAsState()
    val isRefreshingPrices by viewModel.isRefreshingPrices.collectAsState()

    val currentFocusedQuote = quotesMap[focusMarket] ?: symbolList.firstOrNull { it.symbol == focusMarket }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onLiveSignalImageSelected(uri)
        }
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Reusable Top Header (VOWIIai EA | NEURAL KEY: I + Timestamp)
        VowiiHeader(neuralKey = "NEURAL KEY: I")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 95.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =========================================================
            // 5-MINUTES TIMER & BACKEND BRAIN PROCESS LOGS
            // =========================================================
            item {
                val minutes = timerSeconds / 60
                val seconds = timerSeconds % 60
                val timerFormatted = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
                val progress = (300 - timerSeconds) / 300f

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0C101A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "5-Min Timer",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "5-MINUTES BRAIN CYCLE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Auto-feeds live data & news to 8 models for trade signal",
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Digital Glowing Countdown Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CanvasBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(if (analysisSession.isRunning) NeonAmber else NeonGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = timerFormatted,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar showing cycle progress
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = NeonCyan,
                            trackColor = Color(0xFF1E2638)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Changing Single-Sentence Process Logs Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF070B14),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2B3F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (analysisSession.isRunning) NeonAmber else NeonGreen, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "BACKEND BRAIN PROCESS LOG",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp,
                                            color = if (analysisSession.isRunning) NeonAmber else NeonCyan
                                        )
                                    }

                                    Text(
                                        text = if (analysisSession.isRunning) "ANALYZING..." else "ACTIVE SYNC",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Single sentence process log confirming what's happening
                                Text(
                                    text = brainProcessStatus,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Actions: Force Brain Sync
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.forceBrainCycleSync() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = NeonCyan
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "SYNC BRAIN NOW (RESET 5-MIN)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================
            // NEW SECTION ON TOP: "MODELS ANALYSIS"
            // =========================================================
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonGreen
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0x3300FF88), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MODELS ANALYSIS",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "8 Specialized AI Brains • Live MT5, Ticks & News Telemetry",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // List of All Models Used by the Brain
                        Text(
                            text = "ACTIVE ENSEMBLE MODELS IN THE BRAIN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val brainModels = listOf(
                            Pair("Gemini 2.0 Flash", "Google AI"),
                            Pair("DeepSeek R1", "OpenRouter"),
                            Pair("DeepSeek R1 671B", "NVIDIA"),
                            Pair("GPT-4o Mini", "BazaarLink"),
                            Pair("Nemotron-4 340B", "NVIDIA"),
                            Pair("Command R+", "Cohere"),
                            Pair("Space Bunny Alpha", "UnoRouter"),
                            Pair("Nemotron-3 Ultra 550B", "UnoRouter")
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(brainModels) { (name, provider) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(NeonGreen, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "($provider)",
                                            fontSize = 8.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Market Dropdown Selection
                        Text(
                            text = "CHOOSE MARKET FOR FOCUSED AI ANALYSIS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { dropdownExpanded = true },
                                color = SurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "FOCUS MARKET: $focusMarket",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Market",
                                        tint = NeonCyan
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .background(Color(0xFF0E111A))
                                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
                            ) {
                                availableMarkets.forEach { mkt ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = when (mkt) {
                                                    "XAUUSD" -> "XAUUSD (Gold / US Dollar)"
                                                    "BTCUSD" -> "BTCUSD (Bitcoin / USD)"
                                                    "ETHUSD" -> "ETHUSD (Ethereum / USD)"
                                                    "EURUSD" -> "EURUSD (Euro / USD)"
                                                    "GBPUSD" -> "GBPUSD (British Pound / USD)"
                                                    "USDJPY" -> "USDJPY (US Dollar / Yen)"
                                                    "SOLUSD" -> "SOLUSD (Solana / USD)"
                                                    "BNBUSD" -> "BNBUSD (Binance Coin / USD)"
                                                    "NVDA" -> "NVDA (NVIDIA Corporation)"
                                                    "AAPL" -> "AAPL (Apple Inc.)"
                                                    else -> mkt
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = if (mkt == focusMarket) FontWeight.Black else FontWeight.Normal,
                                                color = if (mkt == focusMarket) NeonGreen else Color.White
                                            )
                                        },
                                        onClick = {
                                            viewModel.setModelsFocusMarket(mkt)
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Telemetry Banner for Focused Market
                        currentFocusedQuote?.let { q ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x1AFFFFFF),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "LIVE SPOT PRICE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "$${q.price}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${if (q.changePercent >= 0) "+" else ""}${q.changePercent}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (q.changePercent >= 0) NeonGreen else NeonRed
                                        )
                                        Text(
                                            text = "MT5 #8849201 (Deriv)",
                                            fontSize = 9.sp,
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Latest Live Forex / Macro News preview
                        if (newsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Newspaper,
                                    contentDescription = null,
                                    tint = NeonGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = newsList.first().headline,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chart Screenshot Attachment for 8-Model Vision
                        if (!analysisSession.chartImageUri.isNullOrEmpty() || analysisSession.chartImageAnalyzed) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x2200E5FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { photoLauncher.launch("image/*") }
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
                                            text = "CHART SCREENSHOT ATTACHED • 8 MODELS VISION ACTIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeonCyan,
                                            letterSpacing = 0.8.sp
                                        )
                                        Text(
                                            text = analysisSession.visualChartNotes ?: "Candlestick structures & order blocks read by AI models. Tap to change image.",
                                            fontSize = 9.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        } else {
                            OutlinedButton(
                                onClick = { photoLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Attach Chart Screenshot for 8-Model Vision",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // "RUN AI ANALYSIS" Button
                        Button(
                            onClick = { viewModel.runFullModelsAnalysis() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = CanvasBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (analysisSession.isRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CanvasBackground,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "MODELS ANALYSING $focusMarket...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CanvasBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RUN AI ANALYSIS FOR $focusMarket",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Decisions Made by Each Model
                        if (analysisSession.decisions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "INDIVIDUAL MODEL DECISIONS & AI REASONING",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Real-time signals & reasoning freshly generated from model outputs",
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.runFullModelsAnalysis() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Fetch New Outputs",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "FETCH NEW SIGNALS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                analysisSession.decisions.forEach { dec ->
                                    ModelDecisionItemCard(decision = dec)
                                }
                            }

                            // The Most Favoured Decision: FINAL TRADING SIGNAL CARD
                            analysisSession.finalSignal?.let { finalAction ->
                                Spacer(modifier = Modifier.height(16.dp))

                                val finalColor = when (finalAction) {
                                    SignalAction.BUY -> NeonGreen
                                    SignalAction.SELL -> NeonRed
                                    SignalAction.WAIT -> NeonAmber
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = finalColor.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, finalColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "FINAL TRADING SIGNAL (MOST FAVOURED)",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextMuted,
                                                    letterSpacing = 1.sp
                                                )
                                                Text(
                                                    text = "${finalAction.name} $focusMarket",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = finalColor
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = finalColor.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "${analysisSession.finalConfidence}% CONFIDENCE",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = finalColor
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = analysisSession.consensusSummary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Targets Grid
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            MiniTargetPill("ENTRY", "$${analysisSession.entryPrice}", Color.White)
                                            MiniTargetPill("STOP LOSS", "$${analysisSession.stopLoss}", NeonRed)
                                            MiniTargetPill("TAKE PROFIT", "$${analysisSession.takeProfit}", NeonGreen)
                                            MiniTargetPill("R:R", analysisSession.riskReward, NeonGold)
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Option Button to Execute on Linked MT5
                                        Button(
                                            onClick = {
                                                viewModel.executeModelsFinalSignalOnMT5()
                                                viewModel.selectTab(3)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = finalColor,
                                                contentColor = CanvasBackground
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FlashOn,
                                                contentDescription = null,
                                                tint = CanvasBackground,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "EXECUTE TRADE ON LINKED MT5 (#8849201)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section Title for Watchlist & Finnhub Dual-Feed Price Engine Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x1A00E5FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "FINNHUB REAL-TIME PRICE ENGINE ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Cross-validating Finnhub vs TwelveData & Live Feeds in Real-Time",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.refreshMarketPrices() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = CanvasBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            if (isRefreshingPrices) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    color = CanvasBackground,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Refreshing...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Real-Time Data",
                                    tint = CanvasBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Refresh",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "MARKET WATCHLIST & LIVE FEEDS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TAP A SYMBOL FOR DEEP CHART & ARENA VIEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Live Symbol Cards List
            items(symbolList, key = { it.symbol }) { quote ->
                LiveSignalCard(
                    quote = quote,
                    onSelect = { viewModel.openSymbolDetail(quote.symbol) }
                )
            }
        }
    }
}

@Composable
private fun ModelDecisionItemCard(decision: ModelBrainDecision) {
    val actionColor = when (decision.action) {
        SignalAction.BUY -> NeonGreen
        SignalAction.SELL -> NeonRed
        SignalAction.WAIT -> NeonAmber
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = decision.modelName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "• ${decision.provider}",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = actionColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${decision.action.name} (${decision.confidence}%)",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = actionColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = decision.reasoning,
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Key: ${decision.keySignal}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )

                Text(
                    text = "${decision.latencyMs}ms",
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NeonGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(NeonGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FINNHUB VERIFIED PRICE INGEST",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF141A28)
                ) {
                    Text(
                        text = "NEW REASONING OUTPUT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniTargetPill(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

@Composable
private fun LiveSignalCard(
    quote: SymbolQuote,
    onSelect: () -> Unit
) {
    val isNegative = quote.changePercent < 0
    val trendColor = if (isNegative) NeonRed else NeonGreen
    val displaySymbol = remember(quote.symbol) {
        if (!quote.symbol.contains("/")) quote.symbol.replace("USD", "/USD") else quote.symbol
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect),
        color = Color(0xFF0A0D15),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Symbol + Badge vs Price + Change Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = displaySymbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x2200E5FF)
                    ) {
                        Text(
                            text = quote.assetClass.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val formattedPrice = String.format("%,.2f", quote.price).replace(".00", "")
                    Text(
                        text = formattedPrice,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isNegative) Color(0x33FF4444) else Color(0x3300FF88)
                    ) {
                        Text(
                            text = "${if (isNegative) "" else "+"}${quote.changePercent}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sparkline Graph Canvas
            CardSparklineCanvas(
                priceHistory = quote.priceHistory,
                lineColor = trendColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x1A00FF88),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300FF88))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(NeonGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (quote.comparedWithFinnhub) "FINNHUB VERIFIED" else quote.primarySource,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "REAL-TIME PRICE SYNC",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF072115),
                        contentColor = NeonGreen
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .border(1.dp, NeonGreen, RoundedCornerShape(10.dp)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ANALYZE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CardSparklineCanvas(
    priceHistory: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (priceHistory.size < 2) return@Canvas

        val width = size.width
        val height = size.height

        val minPrice = priceHistory.minOrNull() ?: 0.0
        val maxPrice = priceHistory.maxOrNull() ?: 1.0
        val range = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice

        val points = priceHistory.mapIndexed { index, price ->
            val x = width * index / (priceHistory.size - 1)
            val y = height - (((price - minPrice) / range) * (height * 0.7f) + (height * 0.15f)).toFloat()
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cX = (prev.x + curr.x) / 2f
                cubicTo(cX, prev.y, cX, curr.y, curr.x, curr.y)
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
