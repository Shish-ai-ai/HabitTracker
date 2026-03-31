package com.example.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.habittracker.presentation.HabitsViewModel
import com.example.habittracker.presentation.navigation.Screen
import com.example.habittracker.ui.screens.AddEditHabitScreen
import com.example.habittracker.ui.screens.HabitsScreen
import com.example.habittracker.ui.theme.HabitTrackerTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerTheme {
                MyHabitTrackerApp()
            }
        }
    }
}

@Composable
fun MyHabitTrackerApp() {
    val navController = rememberNavController()
    val viewModel: HabitsViewModel = koinViewModel()

    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Habits
    ) {
        composable<Screen.Habits> {
            val habits by viewModel.habits.collectAsStateWithLifecycle()
            HabitsScreen(
                habits = habits,
                onToggle = viewModel::toggleCompleted,
                onDelete = viewModel::deleteHabit,
                onEditClick = { habit ->
                    navController.navigate(Screen.AddEdit(habit.id))
                },
                onAddClick = {
                    navController.navigate(Screen.AddEdit(null))
                },
                snackbarMessage = snackbarMessage,
                onSnackbarShown = viewModel::clearSnackbarMessage
            )
        }

        composable<Screen.AddEdit> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AddEdit>()
            val habit by viewModel.habitById(route.habitId).collectAsStateWithLifecycle()
            AddEditHabitScreen(
                habit = habit,
                onSave = { name, description ->
                    habit?.let { it -> viewModel.updateHabit(it.id, name, description) } ?: run {
                        viewModel.addHabit(name, description)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}