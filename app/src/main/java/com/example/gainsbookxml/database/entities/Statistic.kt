package com.example.gainsbookxml.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Statistic(
    @PrimaryKey(autoGenerate = true)
    val statisticID: Int,
    val variableID: Int,
    val type: String,
    val value: Double,
    val year: Int,
    val month: Int,
    val day: Int,
)