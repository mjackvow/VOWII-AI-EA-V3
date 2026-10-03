package com.example.data.repository

import com.example.data.local.TradingSignalDao
import com.example.data.local.TradingSignalEntity
import com.example.data.model.EnsembleConsensus
import kotlinx.coroutines.flow.Flow

class SignalRepository(private val dao: TradingSignalDao) {

    val allSignals: Flow<List<TradingSignalEntity>> = dao.getAllSignals()

    suspend fun saveConsensusSignal(consensus: EnsembleConsensus, chartUri: String? = null): Long {
        val entity = TradingSignalEntity(
            symbol = consensus.symbol,
            assetClass = if (consensus.symbol.contains("USD") && !consensus.symbol.contains("BTC")) "FOREX" else "CRYPTO",
            action = consensus.action.name,
            confidence = consensus.overallConfidence,
            entryPrice = consensus.entryPrice,
            stopLoss = consensus.stopLoss,
            takeProfit1 = consensus.takeProfit1,
            takeProfit2 = consensus.takeProfit2,
            takeProfit3 = consensus.takeProfit3,
            riskRewardRatio = consensus.riskRewardRatio,
            rationale = consensus.rationale,
            smcPatterns = consensus.smcPatterns.joinToString(", "),
            wyckoffPhase = consensus.wyckoffPhase,
            timestamp = consensus.timestamp,
            chartUri = chartUri,
            status = "ACTIVE"
        )
        return dao.insertSignal(entity)
    }

    suspend fun deleteSignal(id: Long) {
        dao.deleteSignalById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllSignals()
    }

    suspend fun updateSignalStatus(id: Long, status: String) {
        dao.updateSignalStatus(id, status)
    }
}
