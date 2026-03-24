package com.example.habittracker.presentation.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed interface Screen {
    @Serializable
    data object Habits : Screen

    @Serializable
    data class AddEdit(val habitId: Int?) : Screen
}