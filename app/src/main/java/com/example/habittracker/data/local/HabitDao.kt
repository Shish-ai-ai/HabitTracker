package com.example.habittracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun getAllHabitsSortedByNameAsc(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY name DESC")
    fun getAllHabitsSortedByNameDesc(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE is_completed = 1 ORDER BY name ASC")
    fun getCompletedHabitsSortedByNameAsc(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE is_completed = 1 ORDER BY name DESC")
    fun getCompletedHabitsSortedByNameDesc(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE is_completed = 0 ORDER BY name ASC")
    fun getUncompletedHabitsSortedByNameAsc(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE is_completed = 0 ORDER BY name DESC")
    fun getUncompletedHabitsSortedByNameDesc(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE is_completed = 1")
    fun getCompletedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE is_completed = 0")
    fun getUncompletedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    fun getAllHabitsList(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: String): Flow<HabitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM habits")
    suspend fun deleteAll()
}