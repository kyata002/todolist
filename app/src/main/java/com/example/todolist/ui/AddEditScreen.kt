package com.example.todolist.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.model.Priority
import com.example.todolist.model.Type

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    onSave: (String, String?, Priority, Type) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf<String?>(null) }
    var priority by remember { mutableStateOf(Priority.NORMAL) }
    var typeTask by remember { mutableStateOf(Type.DAY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Task") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title.trim(),
                            note?.takeIf { it.isNotBlank() },
                            priority,
                            typeTask
                        )
                    }
                }
            ) {
                Text("Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = note ?: "",
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text("Priority:", style = MaterialTheme.typography.titleMedium)
            Row {
                Priority.values().forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Type:", style = MaterialTheme.typography.titleMedium)
            Row {
                Type.values().forEach { t ->
                    FilterChip(
                        selected = typeTask == t,
                        onClick = { typeTask = t },
                        label = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}
