package com.example.todolist.model

data class Session(
    val id: Long = 0,
    val taskId: Long?, // null nếu không gắn với task cụ thể
    val startEpoch: Long,
    val endEpoch: Long? = null, // null nếu đang chạy
    val durationMs: Long = 0L,
    val type: SessionType = SessionType.FOCUS
)


enum class SessionType { FOCUS, BREAK }