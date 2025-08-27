package com.example.todolist.model

object PomodoroDefaults {
    const val FOCUS_MIN = 25
    const val BREAK_MIN = 5
    const val LONG_BREAK_MIN = 15
    const val LONG_EVERY = 4 // sau 4 phiên focus thì nghỉ dài
}


data class PomodoroConfig(
    val focusMin: Int = PomodoroDefaults.FOCUS_MIN,
    val breakMin: Int = PomodoroDefaults.BREAK_MIN,
    val longBreakMin: Int = PomodoroDefaults.LONG_BREAK_MIN,
    val longEvery: Int = PomodoroDefaults.LONG_EVERY
)