package com.kkdev.crackie.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FortuneRarity {
    COMMON, GOLDEN, MYSTIC
}

@Entity(tableName = "fortunes")
data class Fortune(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val rarity: FortuneRarity = FortuneRarity.COMMON,
    var wasSeen: Boolean = false,
    var isFavorite: Boolean = false,
    var dateAdded: Long = 0L
)
