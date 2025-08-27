package com.example.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val note: String? = null,
    val isDone: Boolean = false,
    val priority: Priority = Priority.NORMAL,
    val estimateMin: Int? = null,
    val dueEpoch: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)


enum class Priority { LOW, NORMAL, HIGH, VERYHIGH }


