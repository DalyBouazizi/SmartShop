package com.example.smartshop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = "",
    val inCartQuantity: Int = 0,
    val lastModified: Long = System.currentTimeMillis()
)
