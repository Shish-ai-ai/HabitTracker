package com.example.habittracker.data.network

import com.example.habittracker.data.local.AppDatabase
import com.example.habittracker.data.local.toDomainList
import com.example.habittracker.data.local.toEntityList
import com.example.habittracker.domain.DataError
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.HabitRepository
import com.example.habittracker.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlin.collections.emptyList

class HabitRepositoryImpl(
    private val habitApiClient: HttpClient,
    database: AppDatabase
) : HabitRepository {

    private val habitDao = database.habitDao()
    private val localHabits = MutableStateFlow<List<Habit>>(emptyList())

    private suspend fun getLocalHabits(): List<Habit> = habitDao.getAllHabits()
        .map { it.toDomainList() }
        .firstOrNull() ?: emptyList()

    override suspend fun getFilteredAndSortedHabits(
        showCompleted: Boolean?,
        sortOption: SortOption?,
    ): Result<List<Habit>, DataError.Local> = try {
        when {
            showCompleted == true && sortOption == SortOption.NAME_ASC ->
                habitDao.getCompletedHabitsSortedByNameAsc()

            showCompleted == true && sortOption == SortOption.NAME_DESC ->
                habitDao.getCompletedHabitsSortedByNameDesc()

            showCompleted == false && sortOption == SortOption.NAME_ASC ->
                habitDao.getUncompletedHabitsSortedByNameAsc()

            showCompleted == false && sortOption == SortOption.NAME_DESC ->
                habitDao.getUncompletedHabitsSortedByNameDesc()

            sortOption == SortOption.NAME_ASC ->
                habitDao.getAllHabitsSortedByNameAsc()

            sortOption == SortOption.NAME_DESC ->
                habitDao.getAllHabitsSortedByNameDesc()

            else -> habitDao.getAllHabits()
        }
            .map { Result.Success(it.toDomainList()) }
            .firstOrNull()?: Result.Success(emptyList())
    } catch (_: Exception) {
        currentCoroutineContext().ensureActive()
        Result.Error(DataError.Local.UNKNOWN)
    }

    override suspend fun getHabits(): Result<List<Habit>, DataError.Remote> {
        return try {
            val habits = getLocalHabits()

            val result = safeCall<HabitResponseDto> {
                habitApiClient.get("$BASE_URL/habits-tracker")
            }

            when (result) {
                is Result.Success -> {
                    val remoteHabits = result.data.habits.map { it.toDomain() }
                    val merged = mergeHabits(habits, remoteHabits)

                    withContext(Dispatchers.IO) {
                        habitDao.deleteAll()
                        habitDao.insertAll(merged.toEntityList())
                        localHabits.update { merged }
                    }

                    Result.Success(merged)
                }

                is Result.Error -> {
                    if (habits.isNotEmpty()) {
                        Result.Success(habits)
                    } else {
                        result
                    }
                }
            }
        } catch (_: Exception) {
            Result.Error(DataError.Remote.UNKNOWN)
        }
    }

    override suspend fun saveData(habits: List<Habit>) = withContext(Dispatchers.IO) {
        habitDao.deleteAll()
        habitDao.insertAll(habits.toEntityList())
    }

    private fun mergeHabits(
        local: List<Habit>,
        remote: List<Habit>
    ): List<Habit> {
        val localMap = local.associateBy { it.id }.toMutableMap()

        for (remoteHabit in remote) {
            val localHabit = localMap[remoteHabit.id]
            if (localHabit != null) {
                localMap[remoteHabit.id] = remoteHabit.copy(
                    isCompleted = localHabit.isCompleted,
                    streak = localHabit.streak,
                    lastCompletedDate = localHabit.lastCompletedDate
                )
            } else {
                localMap[remoteHabit.id] = remoteHabit
            }
        }

        return localMap.values.toList()
    }

    private fun HabitDto.toDomain() = Habit(
        id = id,
        name = title,
        description = description,
        isCompleted = false,
        streak = streak,
        lastCompletedDate = null,
    )

    companion object {
        private const val BASE_URL = "https://api-labs.wiremockapi.cloud"
    }
}