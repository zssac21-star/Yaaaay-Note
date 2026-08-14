package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CategoryItem
import com.example.data.Note
import com.example.data.StudyFlowDatabase
import com.example.data.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    DASHBOARD,
    NOTES,
    CATEGORIES,
    SETTINGS
}

enum class ViewMode {
    LIST,
    GRID
}

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository
    val database = StudyFlowDatabase.getDatabase(application, viewModelScope)

    init {
        repository = StudyRepository(database.studyDao())
    }

    // Navigation & View States
    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    // Editor state
    private val _editingNote = MutableStateFlow<Note?>(null)
    val editingNote: StateFlow<Note?> = _editingNote.asStateFlow()

    private val _isEditorOpen = MutableStateFlow(false)
    val isEditorOpen: StateFlow<Boolean> = _isEditorOpen.asStateFlow()

    // User Settings State
    private val _userName = MutableStateFlow("Alex Researcher")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("alex@studyflow.app")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _appThemeMode = MutableStateFlow(AppThemeMode.LIGHT)
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    private val _studyRemindersEnabled = MutableStateFlow(true)
    val studyRemindersEnabled: StateFlow<Boolean> = _studyRemindersEnabled.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    // Reactive Data Sources
    val allNotes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allNotes
            } else {
                repository.searchNotes(query)
            }
        }
        .combine(_selectedCategoryFilter) { notes, category ->
            if (category == null || category == "All") {
                notes
            } else {
                notes.filter { it.category.equals(category, ignoreCase = true) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedNotes: StateFlow<List<Note>> = repository.pinnedNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteNotes: StateFlow<List<Note>> = repository.favoriteNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNotes: StateFlow<List<Note>> = repository.getRecentNotes(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryItem>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalNotesCount: StateFlow<Int> = repository.notesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val favoritesCount: StateFlow<Int> = repository.favoritesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categoriesCount: StateFlow<Int> = repository.categoriesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Actions
    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun openNewNoteEditor(defaultCategory: String = "University") {
        val accentColor = when (defaultCategory.lowercase()) {
            "personal" -> "#3525CD"
            "university", "biology" -> "#006C49"
            "work" -> "#684000"
            "ideas" -> "#4F46E5"
            else -> "#3525CD"
        }
        _editingNote.value = Note(
            id = 0,
            title = "",
            content = "",
            category = defaultCategory,
            tags = "",
            isPinned = false,
            isFavorite = false,
            accentColorHex = accentColor,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _isEditorOpen.value = true
    }

    fun openEditNote(note: Note) {
        _editingNote.value = note
        _isEditorOpen.value = true
    }

    fun closeEditor() {
        _isEditorOpen.value = false
        _editingNote.value = null
    }

    fun saveNote(
        title: String,
        content: String,
        category: String,
        tags: String,
        isPinned: Boolean,
        isFavorite: Boolean,
        accentColorHex: String
    ) {
        val current = _editingNote.value
        viewModelScope.launch {
            val noteToSave = if (current != null && current.id != 0L) {
                current.copy(
                    title = title.ifBlank { "Untitled Note" },
                    content = content,
                    category = category,
                    tags = tags,
                    isPinned = isPinned,
                    isFavorite = isFavorite,
                    accentColorHex = accentColorHex,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                Note(
                    title = title.ifBlank { "Untitled Note" },
                    content = content,
                    category = category,
                    tags = tags,
                    isPinned = isPinned,
                    isFavorite = isFavorite,
                    accentColorHex = accentColorHex,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            if (noteToSave.id == 0L) {
                repository.insertNote(noteToSave)
            } else {
                repository.updateNote(noteToSave)
            }
            closeEditor()
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isFavorite = !note.isFavorite))
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
            if (_editingNote.value?.id == note.id) {
                closeEditor()
            }
        }
    }

    fun addCategory(name: String, iconName: String, accentColor: String, description: String = "") {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryItem(
                    name = name.trim(),
                    iconName = iconName,
                    accentColorHex = accentColor,
                    description = description
                )
            )
        }
    }

    fun deleteCategory(category: CategoryItem) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun updateProfile(name: String, email: String) {
        _userName.value = name
        _userEmail.value = email
    }

    fun setThemeMode(mode: AppThemeMode) {
        _appThemeMode.value = mode
    }

    fun setStudyReminders(enabled: Boolean) {
        _studyRemindersEnabled.value = enabled
    }

    fun showNotification(message: String) {
        _notificationMessage.value = message
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun resetDemoData() {
        viewModelScope.launch {
            val all = allNotes.value
            all.forEach { repository.deleteNote(it) }
            val cats = categories.value
            cats.forEach { repository.deleteCategory(it) }

            // Re-seed
            val dao = database.studyDao()
            val now = System.currentTimeMillis()
            val hour = 3600 * 1000L
            val day = 24 * hour

            dao.insertCategory(CategoryItem(name = "Personal", iconName = "person", accentColorHex = "#3525CD"))
            dao.insertCategory(CategoryItem(name = "University", iconName = "school", accentColorHex = "#006C49"))
            dao.insertCategory(CategoryItem(name = "Work", iconName = "work", accentColorHex = "#684000"))
            dao.insertCategory(CategoryItem(name = "Ideas", iconName = "lightbulb", accentColorHex = "#4F46E5"))

            dao.insertNote(
                Note(
                    title = "Neurobiology Ch 4: Synaptic Transmission",
                    content = "Key mechanisms of neurotransmitter release, receptor binding, and signal termination pathways...",
                    category = "Biology",
                    tags = "Biology, Exam Prep",
                    isPinned = true,
                    isFavorite = true,
                    accentColorHex = "#006C49",
                    createdAt = now - 2 * day,
                    updatedAt = now - 1 * hour
                )
            )
            dao.insertNote(
                Note(
                    title = "Thesis: Literature Review Draft",
                    content = "Synthesizing recent papers on minimalist HCI interfaces and cognitive load reduction in academic tools...",
                    category = "Work",
                    tags = "Research",
                    isPinned = true,
                    isFavorite = true,
                    accentColorHex = "#684000",
                    createdAt = now - 3 * day,
                    updatedAt = now - 3 * hour
                )
            )
            dao.insertNote(
                Note(
                    title = "Calculus III: Vector Fields & Line Integrals",
                    content = "Understanding conservative fields, the fundamental theorem for line integrals, and applications in physics...",
                    category = "University",
                    tags = "Math, Week 6",
                    isPinned = false,
                    isFavorite = true,
                    accentColorHex = "#3525CD",
                    createdAt = now - 4 * day,
                    updatedAt = now - 4 * hour
                )
            )
            dao.insertNote(
                Note(
                    title = "Project Ideas for HCI Final",
                    content = "Brainstorming session: 1. A study app focused on flow state, 2. Accessibility checker for web devs...",
                    category = "Ideas",
                    tags = "Ideas",
                    isPinned = false,
                    isFavorite = false,
                    accentColorHex = "#777587",
                    createdAt = now - 5 * day,
                    updatedAt = now - 24 * hour
                )
            )
            dao.insertNote(
                Note(
                    title = "Questions for Professor Miller",
                    content = "Need clarification on the grading rubric for the final essay and the expected word count limits.",
                    category = "University",
                    tags = "Urgent",
                    isPinned = false,
                    isFavorite = false,
                    accentColorHex = "#BA1A1A",
                    createdAt = now - 6 * day,
                    updatedAt = now - 48 * hour
                )
            )
        }
    }
}
