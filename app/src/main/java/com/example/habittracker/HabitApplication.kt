package com.example.habittracker

import android.app.Application
import com.example.habittracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HabitApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@HabitApplication)
            modules(appModule)
        }
    }
}