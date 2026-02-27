package com.tp.tpapp.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class AppWithAccounts(
    @Embedded val app: AppEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "appId"
    )
    val accounts: List<AccountEntity>
)
