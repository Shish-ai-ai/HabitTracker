package com.example.habittracker.data.local

import com.example.habittracker.domain.Habit

fun HabitEntity.toDomain(): Habit {
    return Habit(
        id = id,
        name = name,
        description = description,
        isCompleted = isCompleted,
        streak = streak,
        lastCompletedDate = lastCompletedDate
    )
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        name = name,
        description = description,
        isCompleted = isCompleted,
        streak = streak,
        lastCompletedDate = lastCompletedDate,
        updatedAt = System.currentTimeMillis()
    )
}

fun List<HabitEntity>.toDomainList(): List<Habit> = map { it.toDomain() }

fun List<Habit>.toEntityList(): List<HabitEntity> = map { it.toEntity() }