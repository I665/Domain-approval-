package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "domains")
data class Domain(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String, // e.g. "Tech", "AI", "Arabic", "Finance", "SaaS", "Short"
    val description: String,
    val isPremium: Boolean = false,
    val isFavorite: Boolean = false,
    val isUserAdded: Boolean = false
)
