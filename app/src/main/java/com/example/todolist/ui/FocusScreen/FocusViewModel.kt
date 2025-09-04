package com.example.todolist.ui.FocusScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.TaskRepository
import com.example.todolist.model.Task
import com.example.todolist.model.TaskDao
import com.example.todolist.model.Type
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class FocusViewModel(private val repo: TaskRepository) : ViewModel() {

    val tasks = repo.allTasks
        .map { it.filter { t -> t.typeTask == Type.DAY } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _timeElapsed = MutableStateFlow(0L)
    val timeElapsed: StateFlow<Long> = _timeElapsed

    private var timerJob: Job? = null

    fun startFocusMode() {
        if (timerJob != null) return // tránh tạo nhiều job

        _timeElapsed.value = 0L
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timeElapsed.value += 1
            }
        }
    }

    fun markTaskDone(task: Task) = viewModelScope.launch {
        repo.update(task.copy(isDone = true))
        updateProgress()
    }

    private fun updateProgress() = viewModelScope.launch {
        val list = repo.allTasks.first().filter { it.typeTask == Type.DAY }
        _progress.value = if (list.isEmpty()) 0f else list.count { it.isDone }.toFloat() / list.size
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}




