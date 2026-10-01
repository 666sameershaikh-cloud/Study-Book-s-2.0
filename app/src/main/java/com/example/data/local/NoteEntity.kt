package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val subject: String = "",
    val bookTitle: String = "",
    val chapterTitle: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorTag: Long = 0xFF6366F1
)
