package com.example.habittracker.data.network

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.habittracker.domain.DataError
import com.example.habittracker.domain.Habit
import com.example.habittracker.domain.HabitRepository
import com.example.habittracker.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class HabitRepositoryImpl(
    private val habitApiClient: HttpClient,
    private val sharedPreferences: SharedPreferences,
) : HabitRepository {

    override suspend fun getHabits(): Result<List<Habit>, DataError.Remote> {
        val localHabits = getCachedHabits().orEmpty()

        val result = safeCall<HabitResponseDto> {
            habitApiClient.get("$BASE_URL/habits-tracker")
        }

        return when (result) {
            is Result.Success -> {
                val remoteHabits = result.data.habits.map { it.toDomain() }

                val merged = mergeHabits(localHabits, remoteHabits)

                saveHabits(merged)

                Result.Success(merged)
            }

            is Result.Error -> {
                if (localHabits.isNotEmpty()) {
                    Result.Success(localHabits)
                } else {
                    result
                }
            }
        }
    }

    override suspend fun saveData(habits: List<Habit>) = withContext(Dispatchers.IO) {
        val json = Json.encodeToString(habits)
        sharedPreferences.edit { putString(KEY_HABITS, json) }
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
                    lastCompletedDate = localHabit.lastCompletedDate
                )
            } else {
                localMap[remoteHabit.id] = remoteHabit
            }
        }

        return localMap.values.toList()
    }

    private fun getCachedHabits(): List<Habit>? {
        val json = sharedPreferences.getString(KEY_HABITS, null)
        return json?.let {
            try {
                Json.decodeFromString(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun saveHabits(habits: List<Habit>) {
        try {
            val json = Json.encodeToString(habits)
            sharedPreferences.edit {
                putString(KEY_HABITS, json)
            }
        } catch (_: Exception) {
        }
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
        private const val KEY_HABITS = "KEY_HABITS"
    }
}