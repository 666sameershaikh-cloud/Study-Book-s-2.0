package com.example.data.model

data class EducationCategory(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val subCategories: List<StudySubCategory>
)

data class StudySubCategory(
    val id: String,
    val name: String,
    val categoryId: String,
    val subjects: List<StudySubject>
)

data class StudySubject(
    val id: String,
    val name: String,
    val icon: String,
    val books: List<StudyBook>
)

data class StudyBook(
    val id: String,
    val title: String,
    val author: String,
    val coverColorHex: Long = 0xFF4F46E5,
    val description: String,
    val chapters: List<BookChapter>
)

data class BookChapter(
    val id: String,
    val number: Int,
    val title: String,
    val subjectName: String,
    val summary: String,
    val content: String,
    val estimatedReadMinutes: Int = 10,
    val isCompleted: Boolean = false,
    val progressPercent: Float = 0f
)
