package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appraisals")
data class Appraisal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val domainName: String,
    val estimatedValueRange: String, // e.g. "$2,500 - $4,200"
    val score: Int, // 0 - 100 domain score
    val positives: String, // Comma or newline separated
    val negatives: String, // Comma or newline separated
    val marketTrends: String,
    val suggestedUses: String, // Comma or newline separated
    val timestamp: Long = System.currentTimeMillis()
)
