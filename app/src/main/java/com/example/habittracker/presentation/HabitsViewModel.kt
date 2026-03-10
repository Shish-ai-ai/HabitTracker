package com.example.habittracker.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.habittracker.data.Habit

class HabitsViewModel : ViewModel() {
    private val _habits = mutableStateListOf<Habit>()
    val habits: List<Habit> = _habits

    private var nextId = 0

    fun addHabit(name: String, description: String) {
        val habit = Habit(id = nextId++, name = name, description = description)
        _habits.add(habit)
    }

    fun updateHabit(id: Int, name: String, description: String) {
        val index = _habits.indexOfFirst { it.id == id }
        if (index != -1) {
            _habits[index] = _habits[index].copy(name = name, description = description)
        }
    }

    fun deleteHabit(habit: Habit) {
        _habits.remove(habit)
    }

    fun toggleCompleted(habit: Habit) {
        val index = _habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            _habits[index] = habit.copy(isCompleted = !habit.isCompleted)
        }
    }
}