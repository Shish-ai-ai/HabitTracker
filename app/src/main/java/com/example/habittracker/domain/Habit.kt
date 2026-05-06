package com.example.habittracker.domain

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: String,
    val name: String,
    val description: String,
    val isCompleted: Boolean = false,
)