package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import com.example.data.model.*
import com.example.ui.TradingViewModel
import com.example.ui.components.ConsensusBar
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*

@Composable
fun SymbolDetailScreen(
    symbol: String,
    viewModel: TradingViewModel,
    onBack: () -> Unit
) {
    val quotes by viewModel.symbolQuotes.collectAsState()
    val timeframe by viewModel.selectedTimeframe.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val consensus by viewModel.liveConsensus.collectAsState()
    val indicators by viewModel.currentIndicators.collectAsState()
    val orderBook by viewModel.binanceOrderBook.collectAsState()
    val cotData by viewModel.cotData.collectAsState()

    val quote = quotes[symbol] ?: SymbolQuote(
        symbol = symbol,
        name = "Market Symbol",
        price = 64880.0,
        change24h = -1030.0,
        changePercent = -1.57,
        high24h = 66000.0,
        low24h = 64100.0,
        volume = "$12B",
        assetClass = AssetClass.CRYPTO,
        priceHistory = listOf(66000.0, 65800.0, 65200.0, 64500.0, 65100.0, 64200.0, 64880.0)
    )

    val isNegative = quote.changePercent < 0
    val trendColor = if (isNegative) NeonRed else NeonGreen

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = quote.symbol.replace("USD", "/USD"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceCard
                    ) {
                        Text(
                            text = quote.assetClass.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "BINANCE SPOT LIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Live Price & Metrics Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SurfaceCardBorder
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT SPOT PRICE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${quote.price}",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0x2200FF88),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4400FF88))
                                ) {
                                    Text(
                                        text = if (quote.comparedWithFinnhub) "FINNHUB VERIFIED" else quote.primarySource,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = trendColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, trendColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${if (!isNegative) "+" else ""}${quote.changePercent}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = trendColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill("24H HIGH", "$${quote.high24h}")
                        MetricPill("24H LOW", "$${quote.low24h}")
                        MetricPill("VOLUME", quote.volume)
                        MetricPill("ORDER FLOW", "${orderBook?.imbalancePercent?.toInt() ?: 52}% BIDS")
                    }
                }
            }
        }

        // Timeframe Selector
        item {
            val timeframes = listOf("M5", "M15", "M30", "H1", "H4", "D1")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeframes.forEach { tf ->
                    val isSelected = tf == timeframe
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setTimeframe(tf) },
                        color = if (isSelected) SurfaceCard else CanvasBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeonGreen else SurfaceCardBorder
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = tf,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) NeonGreen else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Interactive Canvas Chart
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SurfaceCardBorder
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${quote.symbol} / USD REAL-TIME TICK FEED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "VWAP: $${indicators.vwap} | ATR: ${indicators.atr}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeonGold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SmoothLineChartCanvas(
                        priceHistory = quote.priceHistory,
                        lineColor = trendColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        // REX-AI Quantitative Intelligence Card (from alexcolls/rex-ai)
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.5f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REX-AI QUANTITATIVE TELEMETRY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x3300E5FF)
                        ) {
                            Text(
                                text = indicators.marketRegime.name.replace("_", " "),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill("EMA 20/50", "$${indicators.ema20} / $${indicators.ema50}")
                        MetricPill("EMA 100/200", "$${indicators.ema100} / $${indicators.ema200}")
                        MetricPill("RSI (14)", "${indicators.rsi} (${indicators.rsiDivergence})")
                        MetricPill("GARCH VOLATILITY", indicators.volatilityGarchRegime)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    cotData?.let { cot ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COT Institutional Net: ${cot.speculativeBias} (${cot.institutionalSentimentScore}% Bullish Sentiment)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Binance Spot API Order Book Depth Ladder (from binance-spot-api-docs)
        item {
            orderBook?.let { book ->
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonGold.copy(alpha = 0.4f)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BINANCE SPOT ORDER BOOK DEPTH (/api/v3/depth)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Imbalance: ${book.imbalancePercent}% Bids",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (book.imbalancePercent >= 50) NeonGreen else NeonRed
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Depth comparison bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(book.imbalancePercent.toFloat().coerceIn(5f, 95f))
                                    .fillMaxHeight()
                                    .background(NeonGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .weight((100f - book.imbalancePercent.toFloat()).coerceIn(5f, 95f))
                                    .fillMaxHeight()
                                    .background(NeonRed)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Top 3 Bids
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BIDS (BUY)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                book.bids.take(3).forEach { bid ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$${bid.price}", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                                        Text("${bid.quantity}", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Top 3 Asks
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ASKS (SELL)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonRed)
                                book.asks.take(3).forEach { ask ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$${ask.price}", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                                        Text("${ask.quantity}", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Trigger Button
        item {
            Button(
                onClick = { viewModel.runEnsembleAnalysisForCurrentSymbol() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x3300FF88),
                    contentColor = NeonGreen
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = NeonGreen,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "DEBATING ACROSS 5 AI AGENTS...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        letterSpacing = 1.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "TRIGGER NOFX AI ARENA DEBATE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Multi-Model Ensemble Consensus Card
        if (consensus != null) {
            item {
                val actionColor = when (consensus!!.action) {
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
                            Text(
                                text = "CONSENSUS VERDICT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.5.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = actionColor.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, actionColor)
                            ) {
                                Text(
                                    text = "${consensus!!.action} (${consensus!!.overallConfidence}%)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = actionColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ConsensusBar(votes = consensus!!.votes)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Take Profit targets (REX-AI TP1, TP2, TP3)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TradeDetailPill("Entry", "$${consensus!!.entryPrice}", Color.White)
                            TradeDetailPill("Stop Loss", "$${consensus!!.stopLoss}", NeonRed)
                            TradeDetailPill("TP1 (50%)", "$${consensus!!.takeProfit1}", NeonGreen)
                            TradeDetailPill("TP2 (30%)", "$${consensus!!.takeProfit2}", NeonCyan)
                            TradeDetailPill("TP3 (20%)", "$${consensus!!.takeProfit3}", NeonGold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = consensus!!.rationale,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.saveSignalToArchives(consensus!!) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                            ) {
                                Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Archive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.pushSignalToMT5(
                                        symbol = consensus!!.symbol,
                                        action = consensus!!.action.name,
                                        confidence = consensus!!.overallConfidence,
                                        sl = consensus!!.stopLoss,
                                        tp = consensus!!.takeProfit1
                                    )
                                    viewModel.selectTab(3)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CanvasBackground)
                            ) {
                                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EXECUTE LIVE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // NOFX AI Debate Arena (from NoFxAiOS/nofx)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Forum, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NOFX MULTI-AI DEBATE ARENA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Text("Live Model Dialogue", fontSize = 10.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    consensus!!.debatePoints.forEach { point ->
                        DebateCard(point)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DebateCard(point: AIDebatePoint) {
    val stanceColor = when (point.stance) {
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
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = point.speakerModel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = stanceColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${point.stance} ${point.confidence}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = stanceColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Bull Thesis: ${point.bullThesis}",
                fontSize = 10.sp,
                color = Color(0xFFC0FFD8),
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Bear Risk: ${point.bearThesis}",
                fontSize = 10.sp,
                color = Color(0xFFFFB3B3),
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Guardrail: ${point.riskWarning}",
                fontSize = 9.sp,
                color = NeonGold,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun MetricPill(title: String, value: String) {
    Column {
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TradeDetailPill(label: String, value: String, valueColor: Color) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SmoothLineChartCanvas(
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

        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * i / gridLines
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        val stepX = width / (priceHistory.size - 1)
        val points = priceHistory.mapIndexed { index, price ->
            val x = index * stepX
            val y = height - (((price - minPrice) / range) * (height * 0.75f) + (height * 0.12f)).toFloat()
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
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx())
        )

        val lastPoint = points.last()
        drawCircle(
            color = lineColor.copy(alpha = 0.4f),
            radius = 6.dp.toPx(),
            center = lastPoint
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = lastPoint
        )
    }
}
