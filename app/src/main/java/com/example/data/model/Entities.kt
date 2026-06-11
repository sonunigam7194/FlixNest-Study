package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val tagline: String,
    val description: String,
    val duration: String,
    val instructor: String,
    val isPremium: Boolean,
    val category: String,
    val lessonsCount: Int,
    val rating: Double,
    val price: Double
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val orderIndex: Int,
    val title: String,
    val duration: String,
    val content: String,
    var isCompleted: Boolean = false
)

@Entity(tableName = "course_progress")
data class CourseProgressEntity(
    @PrimaryKey val courseId: String,
    val isEnrolled: Boolean,
    val isUnlocked: Boolean,
    val currentProgressPercent: Float = 0.0f
)
