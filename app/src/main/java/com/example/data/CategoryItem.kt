package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "folder", // "person", "school", "work", "lightbulb", "folder", "science"
    val accentColorHex: String = "#3525CD",
    val description: String = ""
)
