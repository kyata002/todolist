package com.example.todolist.data

import com.example.todolist.model.Task
import com.example.todolist.model.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    val allTasks: Flow<List<Task>> = dao.getAllTasks()

    suspend fun insert(task: Task) = dao.insertTask(task)

    suspend fun update(task: Task) = dao.updateTask(task)

    suspend fun delete(task: Task) = dao.deleteTask(task)
}
