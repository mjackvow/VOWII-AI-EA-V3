package com.example.data.model

enum class AssetClass {
    CRYPTO, FOREX, INDEX, COMMODITY, STOCK
}

enum class SignalAction {
    BUY, SELL, WAIT
}

enum class MarketRegime {
    TRENDING_BULLISH,
    TRENDING_BEARISH,
    MEAN_REVERTING,
    HIGH_VOLATILITY_SHOCK,
    CONSOLIDATION_SQUEEZE
}

data class SymbolQuote(
    val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val changePercent: Double,
    val high24h: Double,
    val low24h: Double,
    val volume: String,
    val assetClass: AssetClass,
    val priceHistory: List<Double> = emptyList(),
    val primarySource: String = "Finnhub & Multi-Feed",
    val comparedWithFinnhub: Boolean = true,
    val priceConfidence: String = "REAL-TIME VERIFIED"
)

// Real-Time Price Comparison & Cross-Feed Audit Record
data class PriceAuditRecord(
    val symbol: String,
    val finnhubPrice: Double?,
    val secondaryPrice: Double?,
    val secondaryProvider: String,
    val deviationPercent: Double,
    val finalPrice: Double,
    val verificationStatus: String, // e.g. "FINNHUB VERIFIED", "CROSS-CHECKED", "REAL-TIME ALIGNED"
    val timestamp: Long = System.currentTimeMillis()
)

// Binance Spot API Order Book Depth Level (from binance-spot-api-docs /api/v3/depth)
data class OrderBookEntry(
    val price: Double,
    val quantity: Double
)

data class BinanceOrderBook(
    val symbol: String,
    val lastUpdateId: Long = 0,
    val bids: List<OrderBookEntry> = emptyList(),
    val asks: List<OrderBookEntry> = emptyList(),
    val bidLiquidity: Double = 0.0,
    val askLiquidity: Double = 0.0,
    val imbalancePercent: Double = 50.0 // 0-100% (>50% = Buyer Dominance)
)

// REX-AI Commitment of Traders (COT) & Institutional Net Positioning
data class CotData(
    val symbol: String,
    val commercialNetLots: Long,
    val nonCommercialNetLots: Long,
    val speculativeBias: String, // "HEAVY_LONG", "NEUTRAL", "HEAVY_SHORT"
    val institutionalSentimentScore: Int // 0 - 100
)

// REX-AI 50+ Feature Quantitative Telemetry & ARIMA-GARCH Volatility Regime
data class TechnicalIndicators(
    val ema20: Double,
    val ema50: Double,
    val ema100: Double = 0.0,
    val ema200: Double,
    val rsi: Double,
    val rsiDivergence: String = "None", // "Bullish Div", "Bearish Div", "None"
    val macdLine: Double,
    val macdSignal: Double,
    val macdHist: Double = 0.0,
    val atr: Double,
    val upperBollinger: Double,
    val lowerBollinger: Double,
    val bollingerBandwidth: Double = 0.0,
    val bollingerSqueeze: Boolean = false,
    val vwap: Double = 0.0,
    val stochK: Double = 50.0,
    val stochD: Double = 50.0,
    val marketRegime: MarketRegime = MarketRegime.CONSOLIDATION_SQUEEZE,
    val volatilityGarchRegime: String = "Normal" // "Subdued", "Normal", "Elevated", "Extreme Shock"
)

// NOFX Multi-Agent Debate Arena Point (from NoFxAiOS/nofx)
data class AIDebatePoint(
    val speakerModel: String,
    val provider: String,
    val stance: SignalAction,
    val bullThesis: String,
    val bearThesis: String,
    val riskWarning: String,
    val confidence: Int
)

data class ModelVote(
    val modelName: String,
    val provider: String,
    val action: SignalAction,
    val confidence: Int, // 0 - 100
    val rationale: String
)

// Multi-Model Consensus synthesizing NOFX Debate, Binance Order Flow & REX-AI Quant Metrics
data class EnsembleConsensus(
    val symbol: String,
    val action: SignalAction,
    val overallConfidence: Int,
    val consensusScore: Double, // e.g., 0.88
    val votes: List<ModelVote>,
    val debatePoints: List<AIDebatePoint> = emptyList(),
    val modelDecisions: List<ModelBrainDecision> = emptyList(),
    val smcPatterns: List<String>,
    val wyckoffPhase: String,
    val marketRegime: MarketRegime = MarketRegime.TRENDING_BULLISH,
    val cotData: CotData? = null,
    val orderBookImbalance: Double = 50.0,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val takeProfit3: Double,
    val riskRewardRatio: String,
    val rationale: String,
    val chartImageAnalyzed: Boolean = false,
    val visualChartNotes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// NOFX Autopilot & Hard Risk Shield Configuration
data class AutopilotRiskShield(
    val enabled: Boolean = true,
    val maxDailyDrawdownPct: Float = 3.0f,
    val portfolioRiskPerTradePct: Float = 1.5f,
    val autoBreakerTripped: Boolean = false,
    val dailyPnlUsd: Double = 420.50,
    val totalTradesExecuted: Int = 14,
    val activeEngine: String = "NOFX-REX Multi-Exchange Autopilot"
)

// Live Forex & Financial News Item
data class MarketNewsItem(
    val id: String,
    val headline: String,
    val source: String,
    val summary: String,
    val timeAgo: String
)

// Live MT5 Terminal Telemetry Snapshot
data class MT5TerminalSnapshot(
    val login: String = "8849201",
    val server: String = "Deriv-Server-01",
    val balance: Double = 24850.00,
    val equity: Double = 24850.00,
    val freeMargin: Double = 23900.00,
    val pingMs: Int = 12,
    val isConnected: Boolean = true
)

// Individual Brain Model Decision & Detailed Reasoning
data class ModelBrainDecision(
    val modelName: String,
    val provider: String,
    val modelId: String,
    val action: SignalAction,
    val confidence: Int,
    val reasoning: String,
    val keySignal: String,
    val latencyMs: Long = 0L,
    val status: String = "ONLINE",
    val visualChartObservations: String? = null
)

// Models Analysis Section State for Live Signals
data class ModelsAnalysisSession(
    val selectedMarket: String = "XAUUSD",
    val isRunning: Boolean = false,
    val decisions: List<ModelBrainDecision> = emptyList(),
    val finalSignal: SignalAction? = null,
    val consensusSummary: String = "",
    val finalConfidence: Int = 0,
    val entryPrice: Double = 0.0,
    val stopLoss: Double = 0.0,
    val takeProfit: Double = 0.0,
    val riskReward: String = "",
    val newsHeadline: String = "",
    val terminalSummary: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val chartImageUri: String? = null,
    val chartImageAnalyzed: Boolean = false,
    val visualChartNotes: String? = null
)

