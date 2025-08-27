package com.example.todolist

import android.app.Application
import androidx.compose.runtime.Composable
import com.jakewharton.threetenabp.AndroidThreeTen

// Application
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}

// UI entry point
@Composable
fun MyAppUI() {
    // Nội dung Compose
}
