package com.example.habittracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.network.SortOption
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.HabitRepository
import com.example.habittracker.domain.onError
import com.example.habittracker.domain.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class HabitsViewModel(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _filterCompleted = MutableStateFlow<Boolean?>(null)
    val filterCompleted: StateFlow<Boolean?> = _filterCompleted.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    init {
        viewModelScope.launch {
            getData()
        }

        viewModelScope.launch {
            combine(_filterCompleted, _sortOption) { filter, sort ->
                filter to sort
            }.collect(::applyFilterAndSort)
        }
    }

    private fun applyFilterAndSort(filterAndSort: Pair<Boolean?, SortOption>) {
        val showCompleted = filterAndSort.first
        val sortOption = filterAndSort.second
        viewModelScope.launch {
            habitRepository.getFilteredAndSortedHabits(
                showCompleted = showCompleted,
                sortOption = sortOption,
            )
                .onSuccess { habits ->
                    _habits.update { habits }
                }
                .onError { error ->
                    _snackbarMessage.value = "Ошибка загрузки: ${error.name}"
                }
        }
    }

    fun setFilter(showCompleted: Boolean?) {
        _filterCompleted.value = showCompleted
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    private fun saveData() {
        viewModelScope.launch {
            habitRepository.saveData(_allHabits.value)
        }
    }

    private suspend fun getData() {
        habitRepository.getHabits()
            .onSuccess { habits ->
                _allHabits.update { updateDailyCompletionStatus(habits) }
                saveData()
            }
            .onError { error ->
                _snackbarMessage.value = "Ошибка загрузки: ${error.name}"
            }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getData()
            _isRefreshing.value = false
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
        _allHabits
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

        _allHabits.update { old -> old + habit }
        saveData()
        _snackbarMessage.value = "Привычка \"${habit.name}\" добавлена"
    }

    fun updateHabit(id: String, name: String, description: String) {
        _allHabits.update { old ->
            old.map {
                if (it.id == id) it.copy(name = name, description = description)
                else it
            }
        }
        saveData()
        _snackbarMessage.value = "Привычка \"$name\" обновлена"
    }

    fun deleteHabit(habit: Habit) {
        _allHabits.update { old -> old - habit }
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

        _allHabits.update { old ->
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