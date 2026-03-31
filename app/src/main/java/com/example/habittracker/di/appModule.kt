package com.example.habittracker.di

import android.content.Context
import android.content.SharedPreferences
import com.example.habittracker.presentation.HabitsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private const val PREFS_NAME = "habit_shared_prefs"

val appModule = module {

    factory<SharedPreferences> {
        androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    viewModel<HabitsViewModel> {
        HabitsViewModel(
            sharedPreferences = get(),
        )
    }
}