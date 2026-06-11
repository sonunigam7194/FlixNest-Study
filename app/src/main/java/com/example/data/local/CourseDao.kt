package com.example.data.local

import androidx.room.*
import com.example.data.model.CourseEntity
import com.example.data.model.CourseProgressEntity
import com.example.data.model.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Query("SELECT * FROM course_progress WHERE courseId = :courseId LIMIT 1")
    fun getCourseProgressFlow(courseId: String): Flow<CourseProgressEntity?>

    @Query("SELECT * FROM course_progress WHERE courseId = :courseId LIMIT 1")
    suspend fun getCourseProgress(courseId: String): CourseProgressEntity?

    @Query("SELECT * FROM course_progress")
    fun getAllCourseProgress(): Flow<List<CourseProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCourseProgress(progress: CourseProgressEntity)
}
