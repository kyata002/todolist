package com.example.todolist.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.todolist.model.Priority
import com.example.todolist.model.Task
import java.time.ZoneId
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboxScreenWithDB(
    navController: NavHostController,
    viewModel: TaskViewModel
) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    InboxScreen(
        navController = navController,
        tasks = tasks,
        onAddTask = { navController.navigate("add_edit") },
        onDelete = { task -> viewModel.delete(task) },
        onToggleDone = { id -> viewModel.toggleDone(id) }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboxScreen(
    navController: NavHostController,
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onDelete: (Task) -> Unit,
    onToggleDone: (Long) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTask() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Today", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks, key = { it.id }) { task ->
                    TaskRow(task = task, onToggleDone = onToggleDone, onDelete)
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(task: Task, onToggleDone: (Long) -> Unit, onDelete: (Task) -> Unit) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(1.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { /* click thường */ },
                            onLongClick = { showDialog = true },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularCheckbox(
                        checked = task.isDone,
                        onCheckedChange = { showConfirmDialog = true }
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Tiêu đề task
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (task.isDone) MaterialTheme.colorScheme.outline
                                else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Note, nếu có
                        task.note?.let { note ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Due date, nếu có
                        task.dueEpoch?.let { due ->
                            val dueDate = Instant.ofEpochMilli(due)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Due: $dueDate",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (dueDate.isBefore(LocalDate.now()))
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        // Ước lượng thời gian (estimateMin), nếu có
                        task.estimateMin?.let { mins ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Estimated: $mins min",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                }

                // ⚡ Flag priority nhô góc
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .padding(0.dp,8.dp,0.dp,0.dp)
                ) {
                    PriorityFlagSimple(priority = task.priority)
                }
            }
        }
    }




    // Dialog xác nhận
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = "Xác nhận") },
            text = {
                val actionText =
                    if (task.isDone) "đánh dấu chưa hoàn thành" else "đánh dấu hoàn thành"
                Text("Bạn có chắc muốn $actionText công việc này không?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleDone(task.id)
                        showConfirmDialog = false
                    }
                ) {
                    Text("Đồng ý")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
    // Dialog xóa
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Xóa task") },
            text = { Text("Bạn có chắc muốn xóa '${task.title}' không?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(task)
                    showDialog = false
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun CircularCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (checked) checkedColor else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (checked) checkedColor else uncheckedColor,
                shape = CircleShape
            )
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

@Composable
fun PriorityFlagSimple(priority: Priority) {
    val color = when (priority) {
        Priority.LOW -> Color(0xFF4CAF50)
        Priority.NORMAL -> Color(0xFF2196F3)
        Priority.HIGH -> Color(0xFFFFC107)
        Priority.VERYHIGH -> Color(0xFFF44336)
    }

    val widthDp = 16.dp
    val heightRectDp = 10.dp
    val heightTriangleDp = 10.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentSize()
    ) {
        // Hình chữ nhật chỉ màu
        Box(
            modifier = Modifier
                .size(width = widthDp, height = heightRectDp)
                .background(color)
        )

        // Tam giác nhô xuống
        Canvas(
            modifier = Modifier
                .size(width = widthDp, height = heightTriangleDp)
        ) {
            val w = size.width
            val h = size.height
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w / 2f, h)
                    close()
                },
                color = color
            )
        }
    }
}











