package com.example.todolist.ui.InboxScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.todolist.model.Priority
import com.example.todolist.model.Task
import java.time.ZoneId
import java.time.Instant
import java.time.LocalDate
import com.example.todolist.R
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.rememberDismissState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.todolist.model.Type
import com.example.todolist.navigation.Screen
import com.example.todolist.ui.TaskViewModel
import java.time.format.DateTimeFormatter


@Composable
fun InboxScreenWithDB(
    navController: NavHostController, viewModel: TaskViewModel
) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    InboxScreen(
        navController = navController,
        tasks = tasks,
        onAddTask = { navController.navigate("add_edit") },
        onDelete = { task -> viewModel.delete(task) },
        onToggleDone = { id -> viewModel.toggleDone(id) })
}

@Composable
fun InboxScreen(
    navController: NavHostController,
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onDelete: (Task) -> Unit,
    onToggleDone: (Long) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Tasks", "Done", "Overdue")

    // ⭐ Thêm filter theo Type
    var selectedType by remember { mutableStateOf<Type?>(null) } // null = All

    // Dữ liệu sau khi lọc theo type
    val filteredTasks = remember(tasks, selectedType) {
        if (selectedType == null) tasks else tasks.filter { it.typeTask == selectedType }
    }

    val doneTasks = filteredTasks.filter { it.isDone }
    val activeTasks = filteredTasks.filter { task ->
        !task.isDone && Instant.ofEpochMilli(task.createdAt).atZone(ZoneId.systemDefault())
            .toLocalDate() >= LocalDate.now()
    }
    val overdueTasks = filteredTasks.filter { task ->
        !task.isDone && Instant.ofEpochMilli(task.createdAt).atZone(ZoneId.systemDefault())
            .toLocalDate().isBefore(LocalDate.now())
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FloatingActionButton(onClick = { onAddTask() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }

                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.Focus.route) },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Start Focus Mode")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            Text("To Do List", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            // Tabs
            ModernTabRow(
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it })

            Spacer(Modifier.height(12.dp))

            // ⭐ Dropdown lọc theo Type
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart // đẩy nút ra sát phải
            ) {
                var expanded by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(selectedType?.name ?: "All Types")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(IntrinsicSize.Min) // chỉ rộng bằng nút
                ) {
                    DropdownMenuItem(
                        text = { Text("All Types") },
                        onClick = { selectedType = null; expanded = false }
                    )
                    Type.values().forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.name) },
                            onClick = { selectedType = t; expanded = false }
                        )
                    }
                }
            }




            Spacer(Modifier.height(12.dp))

            // Danh sách theo tab
            when (selectedTabIndex) {
                0 -> TaskListGroupedByDate(activeTasks, onToggleDone, onDelete)
                1 -> TaskListGroupedByDate(doneTasks, onToggleDone, onDelete)
                2 -> TaskListGroupedByDate(overdueTasks, onToggleDone, onDelete)
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: Task, onToggleDone: (Long) -> Unit, onDelete: (Task) -> Unit
) {

    val createdDate =
        Instant.ofEpochMilli(task.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()

    val backgroundColor = when {
        task.isDone -> MaterialTheme.colorScheme.surfaceVariant
        createdDate.isBefore(LocalDate.now()) -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .combinedClickable(onClick = {}, onLongClick = {

                    }),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_item),
                    contentDescription = "Task",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(end = 8.dp) // khoảng cách giữa icon và text
                )
                Column(modifier = Modifier.weight(1f)) {


                    Text(
                        text = task.title, style = MaterialTheme.typography.titleMedium.copy(
                            color = if (task.isDone) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                            fontWeight = FontWeight.Bold // ⭐ bôi đậm
                        ), maxLines = 1, overflow = TextOverflow.Ellipsis
                    )

                    task.note?.let { note ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = note, style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ), maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val date = Instant.ofEpochMilli(task.createdAt)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        Text(
                            text = "Created: $date",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when {
                                    task.isDone -> MaterialTheme.colorScheme.outline
                                    date.isBefore(LocalDate.now()) -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant // màu hiển thị tốt trên surface
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val style  = task.typeTask.toString()
                        Text(
                            text = "Style: $style",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant // màu hiển thị tốt trên surface
                            )
                        )

                    }
                }


            }
            PriorityFlagSimple(
                priority = task.priority, modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }

}


@Composable
fun ModernTabRow(
    tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp,0.dp,0.dp,0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            val shape = RoundedCornerShape(24.dp)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(shape) // 👈 bắt ripple theo bo góc
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable(
                        onClick = { onTabSelected(index) },
                        indication = ripple(bounded = true), // 👈 ripple theo shape
                        interactionSource = remember { MutableInteractionSource() }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp, // thay cho Modifier.size(18.dp)
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun PriorityFlagSimple(
    priority: Priority, modifier: Modifier = Modifier // ⭐ thêm modifier mặc định
) {
    val color = when (priority) {
        Priority.LOW -> Color(0xFF4CAF50)
        Priority.NORMAL -> Color(0xFF2196F3)
        Priority.HIGH -> Color(0xFFFFC107)
        Priority.VERYHIGH -> Color(0xFFF44336)
    }

    val widthDp = 16.dp
    val heightRectDp = 18.dp
    val heightTriangleDp = 10.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = modifier
            .wrapContentSize()
            .padding(0.dp, 0.dp, 12.dp, 0.dp)
        // ⭐ dùng modifier ở đây
    ) {
        Box(
            modifier = Modifier
                .size(width = widthDp, height = heightRectDp)
                .background(color)
        )

        Canvas(
            modifier = Modifier.size(width = widthDp, height = heightTriangleDp)
        ) {
            val w = size.width
            val h = size.height
            drawPath(
                path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w / 2f, h)
                    close()
                }, color = color
            )
        }
    }
}


@Composable
fun TaskListGroupedByDate(
    tasks: List<Task>, onToggleDone: (Long) -> Unit, onDelete: (Task) -> Unit
) {
    val grouped = remember(tasks) {
        tasks.groupBy { task ->
            Instant.ofEpochMilli(task.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSortedMap(compareByDescending { it }) // sắp xếp ngày mới → cũ
    }

    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text("No tasks here 🎉", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            grouped.forEach { (date, tasksOfDay) ->
                // Header line
                item(key = "header_$date") {
                    DateHeader(date = date)
                }

                // Các task trong ngày đó
                items(tasksOfDay, key = { it.id }) { task ->
                    SwipeableTaskRow(
                        task = task, onToggleDone = onToggleDone, onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: LocalDate) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    val text = when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        in (today.minusDays(6)..today.minusDays(2)) -> date.dayOfWeek.name.lowercase()
            .replaceFirstChar { it.uppercase() }

        else -> date.format(formatter)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.width(40.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = text, style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(Modifier.width(8.dp))

        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableTaskRow(
    task: Task, onToggleDone: (Long) -> Unit, onDelete: (Task) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberDismissState(
        confirmStateChange = { value ->
            when (value) {
                DismissValue.DismissedToEnd -> { // vuốt sang phải → Done
                    if (!task.isDone) onToggleDone(task.id)
                    true
                }

                DismissValue.DismissedToStart -> { // vuốt sang trái → Delete
                    showDeleteDialog = true
                    false // chưa xóa ngay, đợi confirm
                }

                else -> false
            }
        })

    SwipeToDismiss(state = dismissState, background = {
        val color = when (dismissState.dismissDirection) {
            DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            DismissDirection.EndToStart -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
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
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }, dismissContent = {
        TaskRow(
            task = task, onToggleDone = onToggleDone, onDelete = { showDeleteDialog = true })
    })

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa task") },
            text = { Text("Bạn có chắc muốn xóa '${task.title}' không?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(task)
                    showDeleteDialog = false
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            })
    }
}
















