package com.example.smartshop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = "",          // NEW: image URL
    val rating: Float = 4.5f,           // NEW: rating (0–5)
    val isDealOfTheDay: Boolean = false, // NEW: flag to highlight card
    val inCartQuantity: Int = 0,
    val lastModified: Long = System.currentTimeMillis()


)
