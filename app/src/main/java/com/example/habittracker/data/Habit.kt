package com.example.habittracker.data

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: Int,
    val name: String,
    val description: String,
    val isCompleted: Boolean = false,
)