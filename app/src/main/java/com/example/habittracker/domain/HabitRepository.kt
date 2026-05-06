package com.example.habittracker.domain

interface HabitRepository {

    suspend fun getHabits(): Result<List<Habit>, DataError.Remote>

    suspend fun saveData(habits: List<Habit>)
}