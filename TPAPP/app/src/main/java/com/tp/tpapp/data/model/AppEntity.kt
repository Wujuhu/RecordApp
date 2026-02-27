package com.tp.tpapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val note: String? = null,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false
)
