package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TradingSignalDao {
    @Query("SELECT * FROM trading_signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<TradingSignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: TradingSignalEntity): Long

    @Query("DELETE FROM trading_signals WHERE id = :id")
    suspend fun deleteSignalById(id: Long)

    @Query("DELETE FROM trading_signals")
    suspend fun clearAllSignals()

    @Query("UPDATE trading_signals SET status = :status WHERE id = :id")
    suspend fun updateSignalStatus(id: Long, status: String)
}
