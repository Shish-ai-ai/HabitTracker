package com.example.habittracker.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.habittracker.data.local.AppDatabase
import com.example.habittracker.data.network.HabitRepositoryImpl
import com.example.habittracker.domain.HabitRepository
import com.example.habittracker.presentation.HabitsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private const val PREFS_NAME = "habit_shared_prefs"

val appModule = module {

    factory<SharedPreferences> {
        androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "habits_database"
        ).build()
    }

    single<HttpClientEngine> { OkHttp.create() }

    single<HttpClient> {
        val engine = get<HttpClientEngine>()

        HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    json = Json { ignoreUnknownKeys = true }
                )
            }

            install(HttpTimeout) {
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }

    factory<HabitRepository> {
        HabitRepositoryImpl(
            habitApiClient = get(),
            database = get()
        )
    }

    viewModel<HabitsViewModel> {
        HabitsViewModel(
            habitRepository = get(),
        )
    }
}