package com.example.gerdachat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gerdachat.data.model.Memory
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC LIMIT 10")
    fun getRecentMemories(): Flow<List<Memory>>

    @Query("SELECT fact FROM memories ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentFacts(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory)

    @Query("DELETE FROM memories")
    suspend fun clearMemories()
}
