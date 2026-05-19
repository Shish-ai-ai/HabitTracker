package com.example.habittracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.HabitRepository
import com.example.habittracker.domain.onError
import com.example.habittracker.domain.onSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitsViewModel(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits = _habits.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            getData()
        }
    }

    private fun saveData() {
        viewModelScope.launch {
            habitRepository.saveData(habits.value)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getData()
            _isRefreshing.value = false
        }
    }

    private suspend fun getData() {
        habitRepository.getHabits()
            .onSuccess { habits ->
                _habits.value = updateDailyCompletionStatus(habits)
            }
            .onError { error ->
                _snackbarMessage.value = "Ошибка загрузки: ${error.name}"
            }
    }

    private fun updateDailyCompletionStatus(habits: List<Habit>): List<Habit> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        return habits.map { habit ->
            val lastDate = habit.lastCompletedDate

            if (lastDate == today) {
                habit
            } else {
                val newStreak = calculateStreak(habit, today)
                habit.copy(
                    isCompleted = false,
                    streak = newStreak
                )
            }
        }
    }

    private fun calculateStreak(habit: Habit, today: String): Int {
        val lastDate = habit.lastCompletedDate ?: return habit.streak
        val yesterday = LocalDate.now().minusDays(1)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

        return if (lastDate == yesterday) {
            habit.streak
        } else {
            0
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun habitById(id: String?): StateFlow<Habit?> =
        _habits
            .map { list -> list.firstOrNull { it.id == id } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                null
            )

    @OptIn(ExperimentalUuidApi::class)
    fun addHabit(name: String, description: String) {
        val habit = Habit(
            id = "$Uuid.generateV4()",
            name = name,
            description = description,
            streak = 0,
            lastCompletedDate = null
        )

        _habits.update { old -> old + habit }
        saveData()
        _snackbarMessage.value = "Привычка \"${habit.name}\" добавлена"
    }

    fun updateHabit(id: String, name: String, description: String) {
        _habits.update { old ->
            old.map {
                if (it.id == id) it.copy(name = name, description = description)
                else it
            }
        }
        saveData()
        _snackbarMessage.value = "Привычка \"$name\" обновлена"
    }

    fun deleteHabit(habit: Habit) {
        _habits.update { old -> old - habit }
        saveData()
        _snackbarMessage.value = "Привычка \"${habit.name}\" удалена"
    }

    fun toggleCompleted(habit: Habit) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val lastDate = habit.lastCompletedDate

        if (habit.isCompleted && lastDate == today) {
            _snackbarMessage.value = "\"${habit.name}\" уже отмечена сегодня!"
            return
        }

        val yesterday = LocalDate.now().minusDays(1)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

        val newStreak = if (lastDate == yesterday) {
            habit.streak + 1
        } else {
            1
        }

        _habits.update { old ->
            old.map {
                if (it.id == habit.id) {
                    it.copy(
                        isCompleted = true,
                        streak = newStreak,
                        lastCompletedDate = today
                    )
                } else {
                    it
                }
            }
        }

        saveData()
        _snackbarMessage.value = "\"${habit.name}\" отмечена! Серия: $newStreak дн."
    }
}