package com.example.todolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.TaskRepository
import com.example.todolist.model.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repo: TaskRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repo.allTasks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleDone(taskId: Long) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == taskId } ?: return@launch
            repo.update(task.copy(isDone = !task.isDone))
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repo.insert(task)
        }
    }
    fun delete(task: Task){
        viewModelScope.launch {
            repo.delete(task)
        }
    }
}

class TaskViewModelFactory(private val repo: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
