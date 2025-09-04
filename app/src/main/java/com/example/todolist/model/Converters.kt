package com.example.todolist.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun fromType(type: Type?): String? = type?.name

    @TypeConverter
    fun toType(value: String?): Type? = value?.let { Type.valueOf(it) }
}
