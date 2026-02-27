package com.tp.tpapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tp.tpapp.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE appId = :appId ORDER BY sortOrder ASC, updatedAt DESC")
    fun getAccountsByAppId(appId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Long): Flow<AccountEntity?>

    @Query(
        "SELECT * FROM accounts WHERE username LIKE '%' || :query || '%' " +
                "OR note LIKE '%' || :query || '%' " +
                "OR tags LIKE '%' || :query || '%' ORDER BY sortOrder ASC, updatedAt DESC"
    )
    fun searchAccounts(query: String): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Update
    suspend fun updateAccounts(accounts: List<AccountEntity>)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE appId = :appId")
    suspend fun deleteAccountsByAppId(appId: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM accounts WHERE appId = :appId")
    suspend fun getNextSortOrder(appId: Long): Int
}
