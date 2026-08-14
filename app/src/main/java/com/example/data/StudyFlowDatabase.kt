package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class, CategoryItem::class], version = 1, exportSchema = false)
abstract class StudyFlowDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: StudyFlowDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): StudyFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyFlowDatabase::class.java,
                    "studyflow_database"
                )
                    .addCallback(StudyDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class StudyDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.studyDao())
                }
            }
        }

        suspend fun populateInitialData(dao: StudyDao) {
            // Seed Categories
            val personalCategory = CategoryItem(
                name = "Personal",
                iconName = "person",
                accentColorHex = "#3525CD",
                description = "Personal reflections and daily tasks"
            )
            val universityCategory = CategoryItem(
                name = "University",
                iconName = "school",
                accentColorHex = "#006C49",
                description = "Coursework, lectures, and exam summaries"
            )
            val workCategory = CategoryItem(
                name = "Work",
                iconName = "work",
                accentColorHex = "#684000",
                description = "Research drafts and project outlines"
            )
            val ideasCategory = CategoryItem(
                name = "Ideas",
                iconName = "lightbulb",
                accentColorHex = "#4F46E5",
                description = "Brainstorms, prototypes, and inspiration"
            )
            dao.insertCategory(personalCategory)
            dao.insertCategory(universityCategory)
            dao.insertCategory(workCategory)
            dao.insertCategory(ideasCategory)

            val now = System.currentTimeMillis()
            val hour = 3600 * 1000L
            val day = 24 * hour

            // Seed Notes from Mockup
            val notes = listOf(
                Note(
                    title = "Neurobiology Ch 4: Synaptic Transmission",
                    content = "Key mechanisms of neurotransmitter release, receptor binding, and signal termination pathways. Action potential propagation reaches presynaptic terminal, triggering voltage-gated Ca2+ channels. Vesicles fuse via SNARE complexes to release acetylcholine and glutamate into the synaptic cleft.",
                    category = "Biology",
                    tags = "Biology, Exam Prep",
                    isPinned = true,
                    isFavorite = true,
                    accentColorHex = "#006C49", // secondary
                    createdAt = now - 2 * day,
                    updatedAt = now - 1 * hour
                ),
                Note(
                    title = "Thesis: Literature Review Draft",
                    content = "Synthesizing recent papers on minimalist HCI interfaces and cognitive load reduction in academic tools. Focused on Miller's Law (7±2 items), Fitts's Law for target acquisition, and visual affordances that facilitate deep flow states during uninterrupted reading sessions.",
                    category = "Work",
                    tags = "Research, HCI",
                    isPinned = true,
                    isFavorite = true,
                    accentColorHex = "#684000", // tertiary
                    createdAt = now - 3 * day,
                    updatedAt = now - 3 * hour
                ),
                Note(
                    title = "Calculus III: Vector Fields & Line Integrals",
                    content = "Understanding conservative fields, the fundamental theorem for line integrals, and applications in physics. A vector field F is conservative if curl F = 0 in a simply connected domain. Potential functions f such that grad(f) = F allow path-independent integration.",
                    category = "University",
                    tags = "Math, Week 6",
                    isPinned = false,
                    isFavorite = true,
                    accentColorHex = "#3525CD", // primary
                    createdAt = now - 4 * day,
                    updatedAt = now - 4 * hour
                ),
                Note(
                    title = "Project Ideas for HCI Final",
                    content = "Brainstorming session: 1. A study app focused on flow state, 2. Accessibility checker for web devs, 3. Multimodal gesture-controlled note reader. Selected Option 1 for implementation with clean Material 3 typography and bento layout hierarchy.",
                    category = "Ideas",
                    tags = "Ideas",
                    isPinned = false,
                    isFavorite = false,
                    accentColorHex = "#777587", // outline variant
                    createdAt = now - 5 * day,
                    updatedAt = now - 24 * hour
                ),
                Note(
                    title = "Questions for Professor Miller",
                    content = "Need clarification on the grading rubric for the final essay and the expected word count limits. Also ask about recommended supplementary bibliography on neural network loss convergence landscapes.",
                    category = "University",
                    tags = "Urgent, Office Hours",
                    isPinned = false,
                    isFavorite = false,
                    accentColorHex = "#BA1A1A", // error
                    createdAt = now - 6 * day,
                    updatedAt = now - 48 * hour
                ),
                Note(
                    title = "Introduction to Machine Learning Algorithms",
                    content = "Key concepts including supervised vs unsupervised learning, gradient descent, and basic neural network architectures. Learning rate scheduling and backpropagation chain rule derivations.",
                    category = "University",
                    tags = "Computer Science, ML",
                    isPinned = false,
                    isFavorite = true,
                    accentColorHex = "#3525CD",
                    createdAt = now - 7 * day,
                    updatedAt = now - 2 * hour
                ),
                Note(
                    title = "Cellular Respiration Cycle",
                    content = "Detailed breakdown of glycolysis, the Krebs cycle, and the electron transport chain ATP yield. Glycolysis produces 2 net ATP, Krebs produces 2 ATP + NADH/FADH2, and oxidative phosphorylation yields approximately 30-32 ATP total.",
                    category = "University",
                    tags = "Biology, Exam Prep",
                    isPinned = false,
                    isFavorite = true,
                    accentColorHex = "#006C49",
                    createdAt = now - 8 * day,
                    updatedAt = now - 26 * hour
                ),
                Note(
                    title = "Themes in Modernist Poetry",
                    content = "Analysis of fragmentation, disillusionment, and changing narrative structures in post-WWI literature. Focus on T.S. Eliot's The Waste Land, Ezra Pound's Cantos, and stream-of-consciousness techniques.",
                    category = "Personal",
                    tags = "Literature, Poetry",
                    isPinned = false,
                    isFavorite = false,
                    accentColorHex = "#684000",
                    createdAt = now - 9 * day,
                    updatedAt = now - 72 * hour
                )
            )

            for (note in notes) {
                dao.insertNote(note)
            }
        }
    }
}
