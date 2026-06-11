package com.example.data.repository

import com.example.data.local.CourseDao
import com.example.data.model.CourseEntity
import com.example.data.model.CourseProgressEntity
import com.example.data.model.LessonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CourseRepository(private val courseDao: CourseDao) {
    val allCourses: Flow<List<CourseEntity>> = courseDao.getAllCourses()
    val allProgress: Flow<List<CourseProgressEntity>> = courseDao.getAllCourseProgress()

    fun getLessonsForCourse(courseId: String): Flow<List<LessonEntity>> =
        courseDao.getLessonsForCourse(courseId)

    fun getCourseProgressFlow(courseId: String): Flow<CourseProgressEntity?> =
        courseDao.getCourseProgressFlow(courseId)

    suspend fun getCourseById(courseId: String): CourseEntity? =
        courseDao.getCourseById(courseId)

    suspend fun getLessonById(lessonId: String): LessonEntity? =
        courseDao.getLessonById(lessonId)

    suspend fun enrollInCourse(courseId: String) {
        val existing = courseDao.getCourseProgress(courseId)
        val isPremium = courseDao.getCourseById(courseId)?.isPremium ?: false
        val progress = CourseProgressEntity(
            courseId = courseId,
            isEnrolled = true,
            isUnlocked = existing?.isUnlocked ?: !isPremium,
            currentProgressPercent = existing?.currentProgressPercent ?: 0.0f,
            isFavorite = existing?.isFavorite ?: false
        )
        courseDao.saveCourseProgress(progress)
    }

    suspend fun unlockCourse(courseId: String) {
        val existing = courseDao.getCourseProgress(courseId)
        val progress = CourseProgressEntity(
            courseId = courseId,
            isEnrolled = existing?.isEnrolled ?: false,
            isUnlocked = true,
            currentProgressPercent = existing?.currentProgressPercent ?: 0.0f,
            isFavorite = existing?.isFavorite ?: false
        )
        courseDao.saveCourseProgress(progress)
    }

    suspend fun toggleFavorite(courseId: String) {
        val existing = courseDao.getCourseProgress(courseId)
        val isPremium = courseDao.getCourseById(courseId)?.isPremium ?: false
        val progress = if (existing != null) {
            existing.copy(isFavorite = !existing.isFavorite)
        } else {
            CourseProgressEntity(
                courseId = courseId,
                isEnrolled = false,
                isUnlocked = !isPremium,
                currentProgressPercent = 0.0f,
                isFavorite = true
            )
        }
        courseDao.saveCourseProgress(progress)
    }

    suspend fun completeLesson(lessonId: String, completed: Boolean) {
        val lesson = courseDao.getLessonById(lessonId) ?: return
        lesson.isCompleted = completed
        courseDao.updateLesson(lesson)

        // Now, recalculate course progress percentage
        val courseId = lesson.courseId
        val lessons = courseDao.getLessonsForCourse(courseId)
        val lessonsList = lessons.firstOrNull() ?: return
        val completedCount = lessonsList.count { it.isCompleted }
        val percent = if (lessonsList.isNotEmpty()) {
            (completedCount.toFloat() / lessonsList.size.toFloat()) * 100f
        } else {
            0.0f
        }

        val existingProgress = courseDao.getCourseProgress(courseId)
        if (existingProgress != null) {
            val updatedProgress = existingProgress.copy(
                currentProgressPercent = percent
            )
            courseDao.saveCourseProgress(updatedProgress)
        }
    }

    suspend fun checkAndSeedData() {
        // We will seed the 5 amazing courses if empty
        val existingCourses = courseDao.getAllCourses()
        val firstBatch = existingCourses.firstOrNull()
        if (firstBatch.isNullOrEmpty()) {
            val courses = listOf(
                CourseEntity(
                    id = "mindset_01",
                    title = "The Mindset Shift: Couch to Growth",
                    tagline = "Mental strength, mindfulness & comfort expansion",
                    description = "Change your life from the comfort of your living room. Discover the psychology of comfort, why the 'couch' is a beautiful space for self-reflection, and how to harness your mindset to take actions that align with your ultimate goals.\n\nThis high-impact initial module acts as your foundation for lifelong transformation, proving that you don't need a gym, a massive office, or a high-pressure environment to start redesigning your career and personal life today.",
                    duration = "2 Weeks",
                    instructor = "Coach Adrian Mercer",
                    isPremium = false,
                    category = "Mindset Shift",
                    lessonsCount = 4,
                    rating = 4.8,
                    price = 0.0
                ),
                CourseEntity(
                    id = "career_01",
                    title = "Tech Career Catalyst: Junior to Lead",
                    tagline = "Accelerated senior leadership & diplomatic engineering",
                    description = "Stuck in middle management or junior development roles? The shift to a senior leadership status has almost nothing to do with writing more code, and everything to do with diplomatic communication, active business empathy, and technical project management. This premium course completely demystifies the path to senior status.\n\nLearn directly from active Silicon Valley executive mentors how to position yourself for promotions, design high-impact deliverables, lead teams through constructive engineering architecture debate, and negotiate an attractive package.",
                    duration = "4 Weeks",
                    instructor = "Coach Diana Stone",
                    isPremium = true,
                    category = "Career Success",
                    lessonsCount = 4,
                    rating = 4.9,
                    price = 49.99
                ),
                CourseEntity(
                    id = "financial_01",
                    title = "Financial Sovereignty: Couch Investor",
                    tagline = "Passive investing, compounding wealth & debt freedom",
                    description = "Take absolute control over your financial destiny from your couch. This course strips away high-pressure Wall Street jargon to give you practical, scientific systems for wealth creation.\n\nFrom understanding how to automate the standard 50-30-20 budget without feeling restricted, to choosing diversified low-cost index funds that out-compound high-fee active advisors, this starter course sets you up for lifetime financial peace of mind. Start small, build consistently.",
                    duration = "3 Weeks",
                    instructor = "Coach Marcus Vance",
                    isPremium = false,
                    category = "Personal Growth",
                    lessonsCount = 4,
                    rating = 4.7,
                    price = 0.0
                ),
                CourseEntity(
                    id = "vitality_01",
                    title = "Peak Physical Vitality & Sleep",
                    tagline = "Supercharge daily energy, sleep hygiene & home fitness",
                    description = "True personal development requires a solid physical foundation. If you are waking up exhausted, feeling midday slumps, and struggling to stay active, this optimization blueprint is for you.\n\nDiscover the simple biology of deep stage-4 sleep, how to stock your kitchen with visual raw fuels for high cognitive energy, and how to execute a 15-minute high-intensity home resistance session that boosts cardiovascular health without expensive Gym memberships.",
                    duration = "3 Weeks",
                    instructor = "Coach Sarah Connor",
                    isPremium = true,
                    category = "Personal Growth",
                    lessonsCount = 4,
                    rating = 4.6,
                    price = 29.99
                ),
                CourseEntity(
                    id = "charisma_01",
                    title = "Public Speaking & Charisma Blueprint",
                    tagline = "Master high-impact presenting, stress-control & charm",
                    description = "Do you freeze when all eyes turn to you in a meeting or on package presentations? Speaking is a craft, not a talent. This premium program provides practical frameworks to control your physiology under stress and command attention in any boardroom.\n\nWe design a complete map: breathing routines that reduce stress before walking in, a clean visual structure to frame any speech with magnetic suspense, and precise body language rules that project authority and empathy simultaneously.",
                    duration = "4 Weeks",
                    instructor = "Coach Liam Sterling",
                    isPremium = true,
                    category = "Career Success",
                    lessonsCount = 4,
                    rating = 4.9,
                    price = 39.99
                )
            )

            val lessons = listOf(
                // Mindset_01
                LessonEntity(
                    id = "mindset_01_l1",
                    courseId = "mindset_01",
                    orderIndex = 1,
                    title = "Understanding Your Comfort Zone",
                    duration = "10 Mins",
                    content = "Your couch is a safe space, but also a passive space. In this introductory lesson, we will explore the neurobiology of comfort zones. Why does your brain crave predictability? Why is the concept of a 'couch' a brilliant tool for self-evaluation, and how can we use moments of rest to make intentional micro-commitments?\n\n## Action Steps:\n1. Reflect on 3 behaviors that keep you in passivity.\n2. Write down your 'Couch Vision'—the person you want to become while sitting in self-reflection today."
                ),
                LessonEntity(
                    id = "mindset_01_l2",
                    courseId = "mindset_01",
                    orderIndex = 2,
                    title = "The Power of Intentional Reflection",
                    duration = "12 Mins",
                    content = "How you talk to yourself is the blueprint of what you build. We dive into transforming negative internal dialogues into constructive self-dialogues.\n\nMany think affirmations are mere 'wishful thinking'—we demonstrate the scientific cognitive mechanism of self-verification. By consciously redirecting focus to your strengths, you rewire neural pathways to spot opportunities.\n\n## Exercises:\n- Draft 3 core anchor phrases related to your career or personal goals.\n- Recite them daily for 3 days."
                ),
                LessonEntity(
                    id = "mindset_01_l3",
                    courseId = "mindset_01",
                    orderIndex = 3,
                    title = "Daily Evening Reflection Rituals",
                    duration = "9 Mins",
                    content = "Consistency is the ultimate differentiator. In this lesson, we structure a lightweight, under-5-min evening journal ritual that you can fill out right from your phone before sleeping.\n\n## Reflect on:\n1. What did I learn today?\n2. Where did I act with fear instead of intent?\n3. One gratitude highlight."
                ),
                LessonEntity(
                    id = "mindset_01_l4",
                    courseId = "mindset_01",
                    orderIndex = 4,
                    title = "Micro-habits for Macro Outcomes",
                    duration = "15 Mins",
                    content = "We wrap up this introductory course by studying atomic behavior modification. You don't need a massive 180-degree turn; you need consistent 1% daily increments.\n\nLearn how 'habit-stacking' lets you anchor new, positive behaviors (like reading 5 pages) to already established daily triggers (like having your evening coffee on the couch)."
                ),

                // Career_01
                LessonEntity(
                    id = "career_01_l1",
                    courseId = "career_01",
                    orderIndex = 1,
                    title = "The Diplomatic Engineer",
                    duration = "18 Mins",
                    content = "Your code may be clean, but is your communication constructive? Moving from active implementation to senior lead roles requires outstanding corporate diplomacy.\n\n## Core Concepts:\n- **Cross-functional empathy**: Translating product requirements to architecture design.\n- **Constructive disagreement**: Using the 'Yes, and' format to address architectural issues without attacking coworker pride.\n- **Strategic transparency**: Managing up and keeping leads informed of critical blockers before they delay delivery."
                ),
                LessonEntity(
                    id = "career_01_l2",
                    courseId = "career_01",
                    orderIndex = 2,
                    title = "High-Impact Project Ownership",
                    duration = "22 Mins",
                    content = "How do you pick tasks that get noticed? Senior leaders do not wait for tickets—they identify core business bottlenecks and propose scalable structural answers.\n\nIn this lesson, we design a step-by-step blueprint on presenting active technical initiatives to your leadership: detailing business value, outlining technical risks, and proposing a realistic timeline."
                ),
                LessonEntity(
                    id = "career_01_l3",
                    courseId = "career_01",
                    orderIndex = 3,
                    title = "High-Probability Salary Negotiation",
                    duration = "20 Mins",
                    content = "Negotiation is a collaborative problem-solving exercise, not a battle. We break down the exact phrases and timing to secure substantial compensation increases.\n\n## Key Rules:\n1. Never reveal your number first.\n2. Anchor your value to tangible KPIs (e.g., how you reduced hosting fees by 18%, or boosted user registration flow).\n3. Standard compensation is always a package—include equity, flexible work structures, and professional development budgets."
                ),
                LessonEntity(
                    id = "career_01_l4",
                    courseId = "career_01",
                    orderIndex = 4,
                    title = "Constructive Mentorship",
                    duration = "16 Mins",
                    content = "True leaders raise the average capabilities of their entire team. Learn how to mentor junior engineers effectively without micro-managing. We cover active listening, designing bite-sized scaling projects, and offering actionable feedback loops."
                ),

                // Financial_01
                LessonEntity(
                    id = "financial_01_l1",
                    courseId = "financial_01",
                    orderIndex = 1,
                    title = "Demystifying Index Funds",
                    duration = "13 Mins",
                    content = "92% of high-cost active fund managers fail to beat the standard market index over 15 years. Why pay ridiculous management fees?\n\nThis lesson demystifies low-cost, diversified exchange-traded funds (ETFs) or mutual funds that track broad markets like the S&P 500 or Total Stock Market indexes. Learn the mechanics of automatic indexing and dollar-cost averaging."
                ),
                LessonEntity(
                    id = "financial_01_l2",
                    courseId = "financial_01",
                    orderIndex = 2,
                    title = "The 50/30/20 Budgeting Rule",
                    duration = "11 Mins",
                    content = "Budgeting shouldn't feel like a starvation diet. With the simple, visual 50/30/20 plan, you run your wealth with zero friction:\n- **50% Needs**: Rent, health, bills, groceries.\n- **30% Wants**: Restaurants, entertainment, coffee, comfort.\n- **20% Savings/Investing**: Automated index purchases, paying down high-interest liabilities."
                ),
                LessonEntity(
                    id = "financial_01_l3",
                    courseId = "financial_01",
                    orderIndex = 3,
                    title = "Conquering Bad Debt For Good",
                    duration = "14 Mins",
                    content = "High-interest debt is a severe constraint on your personal growth. In this lesson, we study the two elite math-driven models of debt elimination:\n- **The Debt Avalanche**: Pay highest interest rates first to minimize total long-term interest cost.\n- **The Debt Snowball**: Pay smallest balance first to build emotional and momentum-based wins."
                ),
                LessonEntity(
                    id = "financial_01_l4",
                    courseId = "financial_01",
                    orderIndex = 4,
                    title = "The Compounding Miracle",
                    duration = "15 Mins",
                    content = "Time is your ultimate asset. We review the exponential mathematics of compound interest. See how a modest $150/month investment starting at age 22 aggregates into massive safety nests over forty years, and enjoy peace of mind."
                ),

                // Vitality_01
                LessonEntity(
                    id = "vitality_01_l1",
                    courseId = "vitality_01",
                    orderIndex = 1,
                    title = "The Biology of Deep Sleep",
                    duration = "15 Mins",
                    content = "Sleep is your intellectual foundation. Bad sleep destroys learning potential and causes brain fog. In this lesson, we review circadian biology, ideal room temperatures, screen light mitigation, and why the ultimate evening coach ritual starts 90 minutes before your eyes shut."
                ),
                LessonEntity(
                    id = "vitality_01_l2",
                    courseId = "vitality_01",
                    orderIndex = 2,
                    title = "Nutrition: Raw Fuel Over Sluggish Calories",
                    duration = "16 Mins",
                    content = "Ever experience a massive 3 PM food coma? Your body is reacting to massive insulin spikes. Learn how to design a diet rich in complex slow-digesting carbs, healthy fats, and high-quality protein to support steady, high-bandwidth brain performance."
                ),
                LessonEntity(
                    id = "vitality_01_l3",
                    courseId = "vitality_01",
                    orderIndex = 3,
                    title = "15-Minute Home Resistance Sequence",
                    duration = "15 Mins",
                    content = "No time? No budget? No problem. Resistance training builds posture, supports active skeletal health, and triggers endorphin releases. This lesson outlines a minimalist yet dense full-body circuit containing classic press-ups, squats, planks, and bridges that can be accomplished on your carpet."
                ),
                LessonEntity(
                    id = "vitality_01_l4",
                    courseId = "vitality_01",
                    orderIndex = 4,
                    title = "Active Recovery & Mobility",
                    duration = "11 Mins",
                    content = "Avoid stiffness from prolonged sitting. Learn 5 fundamental mobility holds targeting high-tension areas like your lower back, hip flexors, and neck. Your body will feel lighter and energized."
                ),

                // Charisma_01
                LessonEntity(
                    id = "charisma_01_l1",
                    courseId = "charisma_01",
                    orderIndex = 1,
                    title = "Controlling the Adrenaline Spike",
                    duration = "14 Mins",
                    content = "Your heart rate accelerates, hands sweat, and breath shortens. This is an evolutionary flight response. Discover tactical diaphragmatic 'Box Breathing' (inhale 4s, hold 4s, exhale 4s, hold 4s) used by elite leaders to slow down physiological panic."
                ),
                LessonEntity(
                    id = "charisma_01_l2",
                    courseId = "charisma_01",
                    orderIndex = 2,
                    title = "The Storyteller's Compass",
                    duration = "17 Mins",
                    content = "Data lists bore managers; story arcs capture attention. We construct a 3-part presentation format:\n1. **The Status Quo**: Define the current challenge.\n2. **The Catalyst**: Introduce the proposed resolution.\n3. **The Horizon**: Describe the exciting benefits of adopting it."
                ),
                LessonEntity(
                    id = "charisma_01_l3",
                    courseId = "charisma_01",
                    orderIndex = 3,
                    title = "Vocal Presence & Strategic Silence",
                    duration = "15 Mins",
                    content = "Speeding up is the #1 mistake. We study resonance, visual pacing, and the power of the 'dramatic pause'. Master how to emphasize keywords and use low, authoritative, yet warm vocal tones."
                ),
                LessonEntity(
                    id = "charisma_01_l4",
                    courseId = "charisma_01",
                    orderIndex = 4,
                    title = "Empathetic Body Language",
                    duration = "16 Mins",
                    content = "How do you project competence and approachable warmth? Learn about open posture, controlled hand gestures within the 'communication box' zone, and building eye contact."
                )
            )

            courseDao.insertCourses(courses)
            courseDao.insertLessons(lessons)

            val initialProgress = listOf(
                CourseProgressEntity("mindset_01", isEnrolled = false, isUnlocked = true),
                CourseProgressEntity("career_01", isEnrolled = false, isUnlocked = false),
                CourseProgressEntity("financial_01", isEnrolled = false, isUnlocked = true),
                CourseProgressEntity("vitality_01", isEnrolled = false, isUnlocked = false),
                CourseProgressEntity("charisma_01", isEnrolled = false, isUnlocked = false)
            )
            for (p in initialProgress) {
                courseDao.saveCourseProgress(p)
            }
        }
    }
}
