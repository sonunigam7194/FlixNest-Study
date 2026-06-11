package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.model.CourseEntity
import com.example.data.model.LessonEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseApp(viewModel: CourseViewModel) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "course_list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("course_list") {
                CourseListScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { courseId ->
                        navController.navigate("course_details/$courseId")
                    }
                )
            }
            composable(
                route = "course_details/{courseId}",
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                CourseDetailScreen(
                    courseId = courseId,
                    viewModel = viewModel,
                    onNavigateToLesson = { lessonId ->
                        navController.navigate("lesson_player/$lessonId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "lesson_player/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                LessonPlayerScreen(
                    lessonId = lessonId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun CourseListScreen(
    viewModel: CourseViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    val courses by viewModel.filteredCourses.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    
    val categories = listOf("All", "Personal Growth", "Career Success", "Mindset Shift")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HeaderSection()

        Spacer(modifier = Modifier.height(16.dp))

        // Polished MD3 sleek Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input")
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
            placeholder = { Text("Search classes & strategies...", color = Slate500) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = PrimaryIndigo) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Slate500)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryIndigo,
                unfocusedBorderColor = Slate200,
                focusedTextColor = Slate900,
                unfocusedTextColor = Slate800
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Polished Category Selector Bar matching layout patterns of HTML custom tab selector: bg-slate-200/50 rounded-2xl
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate200.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCat
                    val containerColor = if (isSelected) Color.White else Color.Transparent
                    val contentColor = if (isSelected) PrimaryIndigo else Slate500
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .background(containerColor)
                            .clickable { viewModel.setCategory(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("category_$category")
                    ) {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = contentColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Badge Headers based on active search
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = if (selectedCat == "All") "Mastery Series" else "$selectedCat Core",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Slate900
            )
            Text(
                text = if (selectedCat == "All") "Premium & Free paths" else "Syllabus modules",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Slate500,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (courses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = "No courses found",
                        modifier = Modifier.size(64.dp),
                        tint = Slate300
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No direct training aligns with your query.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses) { courseWithProg ->
                    CourseCard(
                        course = courseWithProg.course,
                        percentComplete = courseWithProg.progress?.currentProgressPercent ?: 0f,
                        isEnrolled = courseWithProg.progress?.isEnrolled ?: false,
                        isUnlocked = courseWithProg.progress?.isUnlocked ?: false,
                        onClick = { onNavigateToDetails(courseWithProg.course.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    // Elegant primary-to-secondary brand gradient
    val gradient = Brush.linearGradient(
        colors = listOf(PrimaryIndigo, SecondaryIndigo)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("header_card")
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, color = Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Psychology learning MindLift icon alignment
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = "CouchCoach psychology icon",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "CouchCoach Platform",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Elite standard mental paradigms & performance.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCard(
    course: CourseEntity,
    percentComplete: Float,
    isEnrolled: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val progressFraction = percentComplete / 100f
    
    // Dual aesthetics directly from design spec:
    // If PREMIUM: bg-slate-900 with rich borders, amber/gold accents, white main text
    // If FREE: light bg-white, soft slate outlines, delicate shadow, indigo accents
    val bgColors = if (course.isPremium) Slate900 else Color.White
    val borderStroke = if (course.isPremium) {
        BorderStroke(1.5.dp, AmberPremium.copy(alpha = 0.8f))
    } else {
        BorderStroke(1.dp, Slate100)
    }
    val mainTextColor = if (course.isPremium) Color.White else Slate900
    val taglineColor = if (course.isPremium) Slate300 else Slate500

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("course_card_${course.id}")
            .shadow(
                elevation = if (course.isPremium) 6.dp else 2.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = bgColors)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (course.isPremium) Color.White.copy(alpha = 0.15f) else LightIndigoBadge,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = course.category.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = if (course.isPremium) Color.White else PrimaryIndigo,
                        letterSpacing = 0.5.sp
                    )
                }

                // Pricing Badge
                if (course.isPremium) {
                    Box(
                        modifier = Modifier
                            .background(AccentGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, AccentGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = "Premium logo",
                                tint = AccentGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PREMIUM ($${course.price})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = AccentGold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2E7D32).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "FREE PATH",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = course.title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = mainTextColor,
                fontFamily = FontFamily.SansSerif,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp
            )

            // Tagline
            Text(
                text = course.tagline,
                fontSize = 13.sp,
                color = taglineColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Meta Info Grid Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating Star",
                        tint = AccentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = course.rating.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = mainTextColor
                    )
                }

                // Duration Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Clock",
                        tint = if (course.isPremium) Slate400 else Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = course.duration,
                        fontSize = 12.sp,
                        color = taglineColor
                    )
                }

                // Modules Count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = "Book",
                        tint = if (course.isPremium) Slate400 else Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${course.lessonsCount} Core Modules",
                        fontSize = 12.sp,
                        color = taglineColor
                    )
                }
            }

            // Enrolled Progress Bar OR Action Guide button
            if (isEnrolled) {
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = if (course.isPremium) Color.White.copy(alpha = 0.08f) else Slate100)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curriculum Progress",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (course.isPremium) AccentGold else PrimaryIndigo
                    )
                    Text(
                        text = "${percentComplete.toInt()}% Completed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = mainTextColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (course.isPremium) AccentGold else PrimaryIndigo,
                    trackColor = if (course.isPremium) Slate800 else Slate100
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (course.isPremium) Color.White.copy(alpha = 0.08f) else Slate50,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (course.isPremium) Color.White.copy(alpha = 0.15f) else Slate100,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isUnlocked) "Enroll & Track Progress" else "Unlock Elite Curriculum",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) (if (course.isPremium) Color.White else PrimaryIndigo) else AccentGold
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "ForwardArrow",
                            tint = if (isUnlocked) (if (course.isPremium) Color.White else PrimaryIndigo) else AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseViewModel,
    onNavigateToLesson: (String) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var course by remember { mutableStateOf<CourseEntity?>(null) }
    
    LaunchedEffect(courseId) {
        course = viewModel.getCourseById(courseId)
    }

    val progress by viewModel.getCourseProgressFlow(courseId).collectAsStateWithLifecycle(initialValue = null)
    val lessons by viewModel.getLessonsForCourse(courseId).collectAsStateWithLifecycle(initialValue = emptyList())

    val isEnrolled = progress?.isEnrolled ?: false
    val isUnlocked = progress?.isUnlocked ?: false

    var showCheckoutDialog by remember { mutableStateOf(false) }

    if (course == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryIndigo)
        }
        return
    }

    val activeCourse = course!!

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(activeCourse.category + " Coaching Framework", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(elevation = 1.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F9FC))
                .padding(horizontal = 16.dp)
                .testTag("detail_scroll"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(1.dp, Slate100, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Star", tint = AccentGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${activeCourse.rating} Peer Verified", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        }

                        if (activeCourse.isPremium) {
                            Box(
                                modifier = Modifier
                                    .background(AccentGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, AccentGold, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("MASTER SERIOUS", color = AmberPremium, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2E7D32).copy(alpha = 0.11f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("STARTER ACCESS", color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = activeCourse.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        lineHeight = 30.sp
                    )
                    
                    Text(
                        text = activeCourse.tagline,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Coach metadata info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate100)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FRAMEWORK INSTRUCTOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.Person, contentDescription = "Coach person", modifier = Modifier.size(16.dp), tint = PrimaryIndigo)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(activeCourse.instructor, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                            }
                        }

                        Column {
                            Text("FRAMEWORK TIMELINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.AccessTime, contentDescription = "Clock duration", modifier = Modifier.size(16.dp), tint = PrimaryIndigo)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(activeCourse.duration, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate800)
                            }
                        }
                    }
                }
            }

            // Enrollment Lock/Unlock Actions
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!isUnlocked) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            border = BorderStroke(1.5.dp, AccentGold),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Lock, contentDescription = "Locked Premium", tint = AccentGold, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Premium Syllabus Lock", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AccentGold)
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Text(
                                    "Unlock lifetime access to elite lesson paradigms, interactive step-by-step masteries, and state progress metrics tracking for a single payment.",
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = Slate300,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showCheckoutDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("unlock_premium_button")
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.WorkspacePremium, contentDescription = "Crown")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Unlock Framework - $${activeCourse.price}", fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    } else {
                        if (!isEnrolled) {
                            Button(
                                onClick = { viewModel.enrollInCourse(activeCourse.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("enroll_course_button")
                                    .height(54.dp)
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Play Circle", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enroll In Free Path", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth().border(1.dp, Slate100, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Active progress", tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Syllabus Matriculation Active", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Slate900)
                                            Text("Curriculum ${((progress?.currentProgressPercent ?: 0f)).toInt()}% complete", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF2E7D32).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("ENROLLED", color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Framework Syllabus Metadata details
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text("FRAMEWORK OVERVIEW & DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Slate500, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeCourse.description,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = Slate800
                    )
                }
            }

            item {
                Divider(color = Slate200, modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CURRICULUM MODULES", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Slate500, letterSpacing = 0.5.sp)
                    Text("${activeCourse.lessonsCount} Master Chapters", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                }
            }

            if (lessons.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryIndigo)
                    }
                }
            } else {
                items(lessons) { lesson ->
                    val canPlay = isUnlocked && isEnrolled
                    
                    LessonItemRow(
                        lesson = lesson,
                        isUnlocked = isUnlocked,
                        isEnrolled = isEnrolled,
                        onClick = {
                            if (canPlay) {
                                onNavigateToLesson(lesson.id)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            courseTitle = activeCourse.title,
            coursePrice = activeCourse.price,
            onDismiss = { showCheckoutDialog = false },
            onPaymentComplete = {
                showCheckoutDialog = false
                viewModel.unlockCourse(activeCourse.id)
                viewModel.enrollInCourse(activeCourse.id)
            }
        )
    }
}

@Composable
fun LessonItemRow(
    lesson: LessonEntity,
    isUnlocked: Boolean,
    isEnrolled: Boolean,
    onClick: () -> Unit
) {
    val isAvailable = isUnlocked && isEnrolled
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isAvailable) { onClick() }
            .testTag("lesson_row_${lesson.id}")
            .shadow(
                elevation = if (isAvailable) 2.dp else 0.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) Color.White else Slate100.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = 1.dp, 
            color = if (lesson.isCompleted) Color(0xFF2E7D32).copy(alpha = 0.4f)
            else Slate100
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Polished chapter indicator with circle shape and colors
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (lesson.isCompleted) Color(0xFF2E7D32) else (if (isAvailable) LightIndigoBadge else Slate200),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (lesson.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            text = lesson.orderIndex.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isAvailable) PrimaryIndigo else Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = lesson.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isAvailable) Slate900 else Slate500
                    )
                    Text(
                        text = "Module Duration: ${lesson.duration}",
                        fontSize = 12.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!isUnlocked) {
                Icon(Icons.Filled.Lock, contentDescription = "Premium Locked Icon", tint = AccentGold, modifier = Modifier.size(18.dp))
            } else if (!isEnrolled) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Enroll first lock", tint = Slate400, modifier = Modifier.size(18.dp))
            } else {
                IconButton(onClick = onClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Start Lesson",
                        tint = if (lesson.isCompleted) Color(0xFF2E7D32) else PrimaryIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LessonPlayerScreen(
    lessonId: String,
    viewModel: CourseViewModel,
    onBack: () -> Unit
) {
    var lesson by remember { mutableStateOf<LessonEntity?>(null) }
    var course by remember { mutableStateOf<CourseEntity?>(null) }

    LaunchedEffect(lessonId) {
        val activeLesson = viewModel.getLessonById(lessonId)
        lesson = activeLesson
        if (activeLesson != null) {
            course = viewModel.getCourseById(activeLesson.courseId)
        }
    }

    if (lesson == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryIndigo)
        }
        return
    }

    val activeLesson = lesson!!

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(course?.title ?: "Classroom Player", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("player_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.shadow(elevation = 1.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F9FC))
                .padding(16.dp)
                .testTag("player_container"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlayerVisualsContainer(lessonTitle = activeLesson.title)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "CHAPTER ${activeLesson.orderIndex} ACTION GUIDE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryIndigo,
                                letterSpacing = 0.5.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = "Clock duration", modifier = Modifier.size(14.dp), tint = Slate500)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(activeLesson.duration, fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = activeLesson.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                item {
                    val lessonCompleted = activeLesson.isCompleted
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (lessonCompleted) Color(0xFFE8F5E9) else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (lessonCompleted) Color(0xFFC8E6C9) else Slate100
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    if (lessonCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Complete check circle",
                                    tint = if (lessonCompleted) Color(0xFF2E7D32) else Slate400,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (lessonCompleted) "Chapter Mastered" else "Active Challenge",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (lessonCompleted) Color(0xFF2E7D32) else Slate900
                                    )
                                    Text(
                                        text = if (lessonCompleted) "You completed training tasks." else "Mark as finished to update path percentages.",
                                        fontSize = 12.sp,
                                        color = if (lessonCompleted) Color(0xFF2E7D32).copy(alpha = 0.8f) else Slate500
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { 
                                    val nextState = !activeLesson.isCompleted
                                    viewModel.completeLesson(activeLesson.id, nextState)
                                    lesson = activeLesson.copy(isCompleted = nextState)
                                },
                                modifier = Modifier.testTag("toggle_complete_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (lessonCompleted) Color(0xFFC62828).copy(alpha = 0.1f) else PrimaryIndigo,
                                    contentColor = if (lessonCompleted) Color(0xFFC62828) else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (lessonCompleted) "Reset" else "Mastered",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Text("STRATEGIC FRAMEWORK CONTENT", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Slate500, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = activeLesson.content,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Slate800
                        )
                    }
                }
            }

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Exit Classroom Player", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun PlayerVisualsContainer(lessonTitle: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Slate900, Slate800)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(PrimaryIndigo.copy(alpha = pulseAlpha), shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(SecondaryIndigo.copy(alpha = 0.25f), shape = CircleShape)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = "Wave",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "AUDIO LESSON STREAM ACTIVE",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = AccentGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = lessonTitle,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    courseTitle: String,
    coursePrice: Double,
    onDismiss: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    
    var isProcessing by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("checkout_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, AccentGold)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isProcessing && !isSuccess) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = "Checkout Card", tint = AccentGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Secure Portal", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Slate900)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close Checkout", tint = Slate500)
                        }
                    }

                    Divider(color = Slate200)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("CURRICULUM ENROLLING IN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.5.sp)
                        Text(courseTitle, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Slate900)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tuition Matrix Access:", fontSize = 13.sp, color = Slate600, fontWeight = FontWeight.Medium)
                            Text("$${coursePrice}", fontWeight = FontWeight.ExtraBold, color = AmberPremium, fontSize = 15.sp)
                        }
                    }

                    Divider(color = Slate200)

                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_name_input"),
                        label = { Text("Cardholder Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 16) cardNumber = it.filter { c -> c.isDigit() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_number_input"),
                        label = { Text("Credit Card Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("1234567812345678") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { if (it.length <= 5) expiryDate = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_expiry_input"),
                            label = { Text("Expiry (MM/YY)") },
                            placeholder = { Text("12/28") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { if (it.length <= 3) cvv = it.filter { c -> c.isDigit() } },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("card_cvv_input"),
                            label = { Text("CVV") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("123") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            isProcessing = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(2000)
                                isProcessing = false
                                isSuccess = true
                                kotlinx.coroutines.delay(1200)
                                onPaymentComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pay_payment_button")
                            .height(50.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        enabled = cardName.isNotBlank() && cardNumber.length >= 12 && expiryDate.isNotBlank() && cvv.length >= 3
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Pay Securely")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authorise & Secure Access", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else if (isProcessing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = AccentGold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Securing Ledger Connection...", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Slate900)
                        Text("Simulating payment gateway transaction API...", fontSize = 12.sp, color = Slate500)
                    }
                } else if (isSuccess) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success tick",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Transaction Certified!", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF2E7D32))
                        Text("Classroom curriculum unlocked successfully.", fontSize = 12.sp, color = Slate500)
                    }
                }
            }
        }
    }
}
