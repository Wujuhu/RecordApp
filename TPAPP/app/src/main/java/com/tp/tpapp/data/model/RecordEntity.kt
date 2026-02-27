package com.tp.tpapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String? = null,
    val content: String,
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false,
    val isCollapsed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
