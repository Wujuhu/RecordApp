package com.tp.tpapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = AppEntity::class,
            parentColumns = ["id"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("appId")]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appId: Long,
    val username: String,
    val password: String,
    val imageUri: String? = null,
    val note: String? = null,
    val tags: String? = null,
    val sortOrder: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
