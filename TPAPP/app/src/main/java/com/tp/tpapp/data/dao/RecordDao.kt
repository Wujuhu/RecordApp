package com.tp.tpapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tp.tpapp.data.model.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query("SELECT * FROM records WHERE isDeleted = 0 ORDER BY sortOrder ASC")
    fun getActiveRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    fun getRecordById(id: Long): Flow<RecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Update
    suspend fun updateRecords(records: List<RecordEntity>)

    // Soft delete one record
    @Query("UPDATE records SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    // Soft delete all active records
    @Query("UPDATE records SET isDeleted = 1, updatedAt = :timestamp WHERE isDeleted = 0")
    suspend fun softDeleteAllActive(timestamp: Long = System.currentTimeMillis())

    // Restore one record
    @Query("UPDATE records SET isDeleted = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restore(id: Long, timestamp: Long = System.currentTimeMillis())

    // Hard delete one record
    @Query("DELETE FROM records WHERE id = :id")
    suspend fun hardDelete(id: Long)

    // Empty recycle bin
    @Query("DELETE FROM records WHERE isDeleted = 1")
    suspend fun emptyTrash()

    // Update collapse state
    @Query("UPDATE records SET isCollapsed = :isCollapsed WHERE id = :id")
    suspend fun updateCollapsed(id: Long, isCollapsed: Boolean)

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM records WHERE isDeleted = 0")
    suspend fun getNextSortOrder(): Int
}
