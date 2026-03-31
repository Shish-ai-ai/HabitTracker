package com.example.habittracker.presentation

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import androidx.core.content.edit

class HabitsViewModel(
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits = _habits.asStateFlow()

    private var nextId = 0

    companion object {
        private const val KEY_HABITS = "key_habits"
    }

    init {
        getData()
    }

    private fun getData() {
        val json = sharedPreferences.getString(KEY_HABITS, null)
        if (json != null) {
            try {
                val list = Json.decodeFromString<List<Habit>>(json)
                _habits.value = list
                nextId = list.maxOfOrNull { it.id }?.plus(1) ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveData() {
        val json = Json.encodeToString(_habits.value)
        sharedPreferences.edit { putString(KEY_HABITS, json) }
    }

    fun habitById(id: Int?): StateFlow<Habit?> =
        _habits
            .map { list -> list.firstOrNull { it.id == id } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                null
            )

    fun addHabit(name: String, description: String) {
        val habit = Habit(id = nextId++, name = name, description = description)
        _habits.update { old ->
            old + habit
        }
        saveData()
    }

    fun updateHabit(id: Int, name: String, description: String) {
        _habits.update { old ->
            old.map {
                if (it.id == id) it.copy(name = name, description = description)
                else it
            }
        }
        saveData()
    }

    fun deleteHabit(habit: Habit) {
        _habits.update { old -> old - habit }
        saveData()
    }

    fun toggleCompleted(habit: Habit) {
        _habits.update { old ->
            old.map {
                if (it.id == habit.id) it.copy(isCompleted = !it.isCompleted)
                else it
            }
        }
        saveData()
    }
}