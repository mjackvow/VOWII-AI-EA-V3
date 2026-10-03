package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trading_signals")
data class TradingSignalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val assetClass: String,
    val action: String, // BUY, SELL, WAIT
    val confidence: Int,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val takeProfit3: Double,
    val riskRewardRatio: String,
    val rationale: String,
    val smcPatterns: String, // comma separated
    val wyckoffPhase: String,
    val timestamp: Long,
    val chartUri: String? = null,
    val status: String = "ACTIVE" // ACTIVE, WON, LOST, CANCELLED
)
