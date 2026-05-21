package com.example.habittracker.domain

import com.example.habittracker.data.network.SortOption

interface HabitRepository {
    suspend fun getHabits(): Result<List<Habit>, DataError.Remote>

    suspend fun getFilteredAndSortedHabits(
        showCompleted: Boolean?,
        sortOption: SortOption?,
    ): Result<List<Habit>, DataError.Local>

    suspend fun saveData(habits: List<Habit>)
}