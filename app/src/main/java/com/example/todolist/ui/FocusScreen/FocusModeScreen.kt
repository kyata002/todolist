package com.example.todolist.ui.FocusScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.model.Task
import com.example.todolist.ui.InboxScreen.TaskRow

@Composable
fun FocusModeScreen(
    viewModel: FocusViewModel,
    onAllTasksDone: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val progress by viewModel.progress.collectAsState()
    val timeElapsed by viewModel.timeElapsed.collectAsState()

    val activeTasks = tasks.filter { !it.isDone }

    // Nếu tất cả task done → gọi callback
    LaunchedEffect(activeTasks.size) {
        if (activeTasks.isEmpty()) {
            onAllTasksDone()
        }
    }

    // Bắt đầu timer
    LaunchedEffect(viewModel) {
        viewModel.startFocusMode()
    }

    val hours = timeElapsed / 3600
    val minutes = (timeElapsed % 3600) / 60
    val seconds = timeElapsed % 60

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Focus Mode", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activeTasks, key = { it.id }) { task ->
                SwipeableFocusTaskRow(task = task, onToggleDone = { viewModel.markTaskDone(it) })
            }
        }
    }
}




@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableFocusTaskRow(
    task: Task,
    onToggleDone: (Task) -> Unit
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { value ->
            when (value) {
                DismissValue.DismissedToEnd -> { // Vuốt phải → Done
                    if (!task.isDone) onToggleDone(task)
                    false // không remove item khỏi danh sách
                }
                DismissValue.DismissedToStart -> { // Vuốt trái → không làm gì
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart), // hỗ trợ cả 2 hướng
        background = {
            val color = when (dismissState.dismissDirection) {
                DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                DismissDirection.EndToStart -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                null -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = when (dismissState.dismissDirection) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                    null -> Alignment.Center
                }
            ) {
                if (dismissState.dismissDirection == DismissDirection.StartToEnd) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Done",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                    // Vuốt trái chỉ đổi màu, không icon hoặc thêm icon khác
                }
            }
        },
        dismissContent = {
            TaskRow(
                task = task,
                onToggleDone = { onToggleDone(task) },
                onDelete = {} // FocusMode không xóa
            )
        }
    )
}
