package com.example.gainsbookxml.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Variable(
    @PrimaryKey(autoGenerate = true)
    val variableID: Int,
    val variableName: String
)