package com.example.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.Habit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onToggle: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onEditClick: (Habit) -> Unit,
    onAddClick: () -> Unit,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onSnackbarShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Habit Tracker") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(habits) { habit ->
                HabitCard(
                    habit = habit,
                    onToggle = { onToggle(habit) },
                    onDelete = { habitToDelete = habit },
                    onEdit = { onEditClick(habit) }
                )
            }
        }
    }
    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Подтверждение") },
            text = { Text("Вы уверены, что хотите удалить эту привычку?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(habit)
                        habitToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { habitToDelete = null }
                ) {
                    Text("Отменить")
                }
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = { onToggle() }
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}