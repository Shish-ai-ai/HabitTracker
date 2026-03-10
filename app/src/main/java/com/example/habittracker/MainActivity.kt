package com.example.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.habittracker.presentation.navigation.Screen
import com.example.habittracker.ui.screens.AddEditHabitScreen
import com.example.habittracker.ui.screens.HabitsScreen
import com.example.habittracker.ui.theme.HabitTrackerTheme
import com.example.habittracker.presentation.HabitsViewModel

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
    val viewModel: HabitsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Habits
    ) {
        composable<Screen.Habits> {
            HabitsScreen(
                habits = viewModel.habits,
                onToggle = { habit -> viewModel.toggleCompleted(habit) },
                onDelete = { habit -> viewModel.deleteHabit(habit) },
                onEditClick = { habit ->
                    navController.navigate(Screen.AddEdit(habit.id))
                },
                onAddClick = {
                    navController.navigate(Screen.AddEdit(null))
                }
            )
        }

        composable<Screen.AddEdit>
        { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AddEdit>()
            val habitId = route.habitId
            AddEditHabitScreen(
                habitId = habitId,
                onSave = { name, description ->
                    habitId?.let { viewModel.updateHabit(habitId, name, description) } ?: run {
                        viewModel.addHabit(name, description)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}