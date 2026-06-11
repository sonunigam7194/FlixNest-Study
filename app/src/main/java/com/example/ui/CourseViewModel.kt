package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CourseEntity
import com.example.data.model.CourseProgressEntity
import com.example.data.model.LessonEntity
import com.example.data.repository.CourseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CourseWithProgress(
    val course: CourseEntity,
    val progress: CourseProgressEntity?
)

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = CourseRepository(database.courseDao())

    val coursesWithProgress: StateFlow<List<CourseWithProgress>> = combine(
        repository.allCourses,
        repository.allProgress
    ) { courses, progresses ->
        courses.map { c ->
            CourseWithProgress(c, progresses.find { p -> p.courseId == c.id })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCourses: StateFlow<List<CourseWithProgress>> = combine(
        coursesWithProgress,
        _selectedCategory,
        _searchQuery
    ) { courses, cat, query ->
        courses.filter { item ->
            val matchesCategory = when (cat) {
                "All" -> true
                "Favorites" -> item.progress?.isFavorite == true
                else -> item.course.category.equals(cat, ignoreCase = true)
            }
            val matchesSearch = if (query.isEmpty()) true else {
                item.course.title.contains(query, ignoreCase = true) ||
                        item.course.tagline.contains(query, ignoreCase = true) ||
                        item.course.description.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.checkAndSeedData()
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(courseId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(courseId)
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            repository.enrollInCourse(courseId)
        }
    }

    fun unlockCourse(courseId: String) {
        viewModelScope.launch {
            repository.unlockCourse(courseId)
        }
    }

    fun completeLesson(lessonId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.completeLesson(lessonId, completed)
        }
    }

    fun getLessonsForCourse(courseId: String): Flow<List<LessonEntity>> {
        return repository.getLessonsForCourse(courseId)
    }

    fun getCourseProgressFlow(courseId: String): Flow<CourseProgressEntity?> {
        return repository.getCourseProgressFlow(courseId)
    }

    suspend fun getCourseById(courseId: String): CourseEntity? {
        return repository.getCourseById(courseId)
    }

    suspend fun getLessonById(lessonId: String): LessonEntity? {
        return repository.getLessonById(lessonId)
    }
}
