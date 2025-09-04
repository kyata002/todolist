package com.example.todolist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todolist.data.TaskRepository
import com.example.todolist.model.AppDatabase
import com.example.todolist.ui.AddEditScreen
import com.example.todolist.model.Task
import com.example.todolist.ui.FocusScreen.FocusModeScreen
import com.example.todolist.ui.FocusScreen.FocusViewModel
import com.example.todolist.ui.InboxScreen.InboxScreenWithDB
import com.example.todolist.ui.TaskViewModel
import com.example.todolist.ui.TaskViewModelFactory

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEdit : Screen("add_edit")
    object Focus : Screen("focus_mode")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            // Khởi tạo database & repository
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val repo = TaskRepository(db.taskDao())

            // Khởi tạo ViewModel
            val viewModel: TaskViewModel = viewModel(
                factory = TaskViewModelFactory(repo)
            )

            // Gọi InboxScreenWithDB
            InboxScreenWithDB(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.AddEdit.route) {
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val repo = TaskRepository(db.taskDao())
            val viewModel: TaskViewModel = viewModel(
                factory = TaskViewModelFactory(repo)
            )

            AddEditScreen(
                onSave = { title, note, priority, typeTask ->
                    viewModel.addTask(
                        Task(
                            title = title,
                            note = note,
                            priority = priority,
                            typeTask = typeTask
                        )
                    )
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Focus.route) {
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val repo = TaskRepository(db.taskDao())

            // Khởi tạo FocusViewModel
            val focusViewModel = FocusViewModel(repo)

            FocusModeScreen(viewModel = focusViewModel){
                navController.popBackStack()
            }
        }

    }
}
