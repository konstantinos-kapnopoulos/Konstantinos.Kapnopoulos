package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cigarette_records")
data class CigaretteRecord(
    @PrimaryKey val date: String, // format "yyyy-MM-dd"
    val count: Int
)
