package com.example.data.repository

import com.example.data.model.BookChapter
import com.example.data.model.EducationCategory
import com.example.data.model.StudyBook
import com.example.data.model.StudySubCategory
import com.example.data.model.StudySubject

object LibraryRepository {

    val categories: List<EducationCategory> by lazy {
        listOf(
            createSchoolCategory(),
            createGraduationCategory(),
            createPostGraduationCategory(),
            createCompetitiveCategory()
        )
    }

    private fun createSchoolCategory(): EducationCategory {
        val classes = (1..12).map { grade ->
            StudySubCategory(
                id = "class_$grade",
                name = "Class $grade",
                categoryId = "school",
                subjects = listOf(
                    StudySubject(
                        id = "math_$grade",
                        name = "Mathematics",
                        icon = "calculate",
                        books = listOf(
                            StudyBook(
                                id = "math_book_$grade",
                                title = "Class $grade Mathematics Foundations",
                                author = "World Books Board",
                                coverColorHex = 0xFF4F46E5,
                                description = "Comprehensive study material and problem sets for Class $grade Mathematics.",
                                chapters = listOf(
                                    BookChapter(
                                        id = "ch_math_${grade}_1",
                                        number = 1,
                                        title = if (grade <= 5) "Numbers & Arithmetic Foundations" else if (grade <= 8) "Rational Numbers & Linear Equations" else "Real Numbers & Polynomials",
                                        subjectName = "Mathematics",
                                        summary = "Understanding fundamentals of number systems, properties, theorems, and practical applications.",
                                        content = "Mathematics is the science of numbers, quantity, and space. In this chapter, we explore how numbers form the bedrock of logical inquiry. From ancient counting systems to real numbers, we investigate algebraic foundations, properties of addition and multiplication, order of operations, and the fundamental theorem of arithmetic. Practical problem sets guide you step-by-step through algebraic simplification, equation solving, and real-world word problems."
                                    ),
                                    BookChapter(
                                        id = "ch_math_${grade}_2",
                                        number = 2,
                                        title = if (grade <= 5) "Shapes, Symmetry & Geometry" else if (grade <= 8) "Quadrilaterals & Practical Geometry" else "Coordinate Geometry & Trigonometry",
                                        subjectName = "Mathematics",
                                        summary = "Geometric visualization, angles, coordinate axes, and spatial analysis.",
                                        content = "Geometry bridges visual perception and algebraic reasoning. We investigate planar figures, angle sum properties, congruency and similarity criteria, and Cartesian coordinates. In higher grades, trigonometric ratios unlock the power to calculate heights, distances, and periodic cycles. Master theorem proofs and solve dimensional challenges with confidence."
                                    )
                                )
                            )
                        )
                    ),
                    StudySubject(
                        id = "science_$grade",
                        name = if (grade <= 8) "General Science" else "Physics & Chemistry",
                        icon = "science",
                        books = listOf(
                            StudyBook(
                                id = "sci_book_$grade",
                                title = "Class $grade Science & Natural Phenomena",
                                author = "National Academic Council",
                                coverColorHex = 0xFF0D9488,
                                description = "Core scientific concepts, laboratory observations, physics laws, and chemical principles.",
                                chapters = listOf(
                                    BookChapter(
                                        id = "ch_sci_${grade}_1",
                                        number = 1,
                                        title = if (grade <= 8) "Matter, Materials & Environment" else "Chemical Reactions & Equations",
                                        subjectName = "Science",
                                        summary = "State transformations, atomic interactions, balancing chemical equations, and types of reactions.",
                                        content = "Matter surrounds us in solid, liquid, and gaseous phases. When atoms rearrange their chemical bonds, matter undergoes physical or chemical transformations. Learn to balance chemical reactions, identify exothermic versus endothermic processes, and understand oxidation-reduction in everyday life."
                                    )
                                )
                            )
                        )
                    ),
                    StudySubject(
                        id = "english_$grade",
                        name = "English Language & Literature",
                        icon = "menu_book",
                        books = listOf(
                            StudyBook(
                                id = "eng_book_$grade",
                                title = "Class $grade Literature & Composition",
                                author = "Literary Studies Dept",
                                coverColorHex = 0xFF9333EA,
                                description = "Reading comprehension, poetic analysis, grammar mastery, and creative essays.",
                                chapters = listOf(
                                    BookChapter(
                                        id = "ch_eng_${grade}_1",
                                        number = 1,
                                        title = "The Art of Critical Reading",
                                        subjectName = "English",
                                        summary = "Reading prose critically, recognizing narrative viewpoints, and building an expressive vocabulary.",
                                        content = "Language empowers human communication and philosophical contemplation. Through selected essays and stories, this chapter guides you to analyze figurative speech, metaphors, rhetorical devices, and theme development."
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }

        return EducationCategory(
            id = "school",
            name = "Class 1–12",
            description = "Elementary, Middle & High School Curriculum",
            iconName = "school",
            subCategories = classes
        )
    }

    private fun createGraduationCategory(): EducationCategory {
        val degrees = listOf(
            "BA" to listOf("History", "Political Science", "English Literature", "Economics"),
            "BCom" to listOf("Financial Accounting", "Business Law", "Taxation", "Auditing"),
            "BSc" to listOf("Physics", "Chemistry", "Mathematics", "Computer Science"),
            "BBA" to listOf("Principles of Management", "Marketing", "Human Resources", "Finance"),
            "BCA" to listOf("Data Structures & Algorithms", "Database Management (DBMS)", "Operating Systems", "Computer Networks"),
            "BE/BTech" to listOf("Data Structures & Algorithms", "Software Engineering", "Computer Architecture", "Artificial Intelligence"),
            "Other Graduation" to listOf("General Studies", "Research Methodology", "Professional Ethics")
        )

        val subCats = degrees.map { (degName, subjectNames) ->
            StudySubCategory(
                id = "grad_${degName.lowercase().replace("/", "_")}",
                name = degName,
                categoryId = "graduation",
                subjects = subjectNames.map { subjName ->
                    StudySubject(
                        id = "grad_subj_${subjName.lowercase().replace(" ", "_")}",
                        name = subjName,
                        icon = "auto_stories",
                        books = listOf(
                            StudyBook(
                                id = "book_${degName}_${subjName.take(5)}",
                                title = "$subjName: Core Principles & Practice",
                                author = "Prof. R. K. Sharma & Editorial Board",
                                coverColorHex = 0xFF2563EB,
                                description = "Comprehensive university textbook covering $subjName syllabus, case studies, and examination problems.",
                                chapters = listOf(
                                    BookChapter(
                                        id = "ch_${degName}_${subjName}_1",
                                        number = 1,
                                        title = "Foundations, Scope & Methodologies of $subjName",
                                        subjectName = subjName,
                                        summary = "Historical evolution, fundamental axioms, paradigm shifts, and theoretical frameworks.",
                                        content = "This foundational chapter introduces $subjName. We study foundational definitions, underlying theoretical models, core terminology, and practical methodologies used by researchers and professionals. Critical review questions follow."
                                    ),
                                    BookChapter(
                                        id = "ch_${degName}_${subjName}_2",
                                        number = 2,
                                        title = "Advanced Applications & Problem Solving",
                                        subjectName = subjName,
                                        summary = "In-depth case studies, analytical proofs, models, and real-world implementation.",
                                        content = "Applying principles to real challenges requires synthesis of multiple theoretical viewpoints. Here we review standard industry case studies, calculate quantitative metrics, and resolve complex edge scenarios."
                                    )
                                )
                            )
                        )
                    )
                }
            )
        }

        return EducationCategory(
            id = "graduation",
            name = "Graduation",
            description = "BA, BCom, BSc, BBA, BCA, BE/BTech & more",
            iconName = "account_balance",
            subCategories = subCats
        )
    }

    private fun createPostGraduationCategory(): EducationCategory {
        val pgDegrees = listOf("MA", "MCom", "MSc", "MBA", "MCA", "ME/MTech", "Other PG")
        val subCats = pgDegrees.map { degName ->
            StudySubCategory(
                id = "pg_${degName.lowercase().replace("/", "_")}",
                name = degName,
                categoryId = "post_graduation",
                subjects = listOf(
                    StudySubject(
                        id = "pg_subj_${degName}_advanced",
                        name = "Advanced Research & Analytical Systems",
                        icon = "psychology",
                        books = listOf(
                            StudyBook(
                                id = "pg_book_${degName}",
                                title = "$degName Master's Advanced Treatise",
                                author = "Academic Research Council",
                                coverColorHex = 0xFF7C3AED,
                                description = "Post-graduate advanced study texts, research methodologies, and doctoral preparation.",
                                chapters = listOf(
                                    BookChapter(
                                        id = "ch_pg_${degName}_1",
                                        number = 1,
                                        title = "Master's Epistemology & Analytical Rigor",
                                        subjectName = degName,
                                        summary = "Advanced qualitative and quantitative methodologies for academic research.",
                                        content = "At the postgraduate level, scholarship requires rigorous examination of foundational assumptions. This chapter details research design, literature synthesis, hypothesis testing, and analytical frameworks essential for master's defense and scholarly publication."
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }

        return EducationCategory(
            id = "post_graduation",
            name = "Post Graduation",
            description = "MA, MCom, MSc, MBA, MCA, ME/MTech & PG programs",
            iconName = "workspace_premium",
            subCategories = subCats
        )
    }

    private fun createCompetitiveCategory(): EducationCategory {
        val exams = listOf(
            "UPSC" to listOf("Indian Polity & Constitution", "Modern Indian History", "Indian Economy", "Geography & Environment"),
            "MPSC" to listOf("General Studies", "Maharashtra History & Culture", "Public Administration"),
            "SSC" to listOf("Quantitative Aptitude", "General Intelligence & Reasoning", "General Awareness", "English Comprehension"),
            "Banking" to listOf("Banking & Financial Awareness", "Data Interpretation", "Logical Reasoning", "Computer Aptitude"),
            "Railway" to listOf("General Science", "Arithmetic", "General Intelligence", "Railway GK"),
            "NEET" to listOf("Physics for Medical Entrance", "Chemistry for Medical Entrance", "Biology & Human Physiology"),
            "JEE" to listOf("JEE Advanced Physics", "Organic & Inorganic Chemistry", "Calculus, Vectors & Algebra"),
            "Other" to listOf("General Knowledge & Current Affairs", "Verbal & Non-Verbal Reasoning")
        )

        val subCats = exams.map { (examName, subjects) ->
            StudySubCategory(
                id = "exam_${examName.lowercase()}",
                name = examName,
                categoryId = "competitive",
                subjects = subjects.map { subjName ->
                    StudySubject(
                        id = "exam_subj_${subjName.lowercase().replace(" ", "_")}",
                        name = subjName,
                        icon = "verified",
                        books = listOf(
                            StudyBook(
                                id = "exam_book_${examName}_${subjName.take(6)}",
                                title = "$examName Complete Guide: $subjName",
                                author = "Competitive Exam Experts & Toppers",
                                coverColorHex = 0xFFD97706,
                                description = "Topper notes, high-yield formulas, historical questions, and conceptual shortcuts for $examName.",
                                chapters = listOf(
                                    BookChapter(
                                        id = "ch_exam_${examName}_${subjName.take(4)}_1",
                                        number = 1,
                                        title = "$subjName: High-Yield Concepts & Key Questions",
                                        subjectName = subjName,
                                        summary = "Essential syllabus breakdown, repetitive patterns, key facts, and rapid-revision notes.",
                                        content = "Preparation for $examName requires focused mastery over $subjName. In this chapter, we condense the most frequently tested concepts, historical question analyses, memory mnemonics, and timed problem-solving strategies to maximize score efficiency."
                                    ),
                                    BookChapter(
                                        id = "ch_exam_${examName}_${subjName.take(4)}_2",
                                        number = 2,
                                        title = "Mock Practice Problems & Detailed Solutions",
                                        subjectName = subjName,
                                        summary = "Exam-pattern mock tests with stepwise explanations and elimination techniques.",
                                        content = "Speed and accuracy determine success. Attempt these high-probability questions under exam conditions. Study the detailed rationale for each answer choice to refine elimination strategies."
                                    )
                                )
                            )
                        )
                    )
                }
            )
        }

        return EducationCategory(
            id = "competitive",
            name = "Competitive Exams",
            description = "UPSC, MPSC, SSC, Banking, Railway, NEET, JEE & more",
            iconName = "military_tech",
            subCategories = subCats
        )
    }

    fun getAllChapters(): List<BookChapter> {
        val list = mutableListOf<BookChapter>()
        for (cat in categories) {
            for (sub in cat.subCategories) {
                for (subj in sub.subjects) {
                    for (book in subj.books) {
                        list.addAll(book.chapters)
                    }
                }
            }
        }
        return list
    }

    fun findChapterById(chapterId: String): BookChapter? {
        return getAllChapters().find { it.id == chapterId }
    }
}
