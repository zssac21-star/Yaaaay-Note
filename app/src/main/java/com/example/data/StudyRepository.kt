package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {

    val allNotes: Flow<List<Note>> = studyDao.getAllNotes()
    val pinnedNotes: Flow<List<Note>> = studyDao.getPinnedNotes()
    val favoriteNotes: Flow<List<Note>> = studyDao.getFavoriteNotes()
    val allCategories: Flow<List<CategoryItem>> = studyDao.getAllCategories()
    val notesCount: Flow<Int> = studyDao.getNotesCount()
    val favoritesCount: Flow<Int> = studyDao.getFavoritesCount()
    val categoriesCount: Flow<Int> = studyDao.getCategoriesCount()

    fun getRecentNotes(limit: Int = 10): Flow<List<Note>> = studyDao.getRecentNotes(limit)

    fun getNotesByCategory(category: String): Flow<List<Note>> = studyDao.getNotesByCategory(category)

    fun getNoteById(id: Long): Flow<Note?> = studyDao.getNoteById(id)

    fun searchNotes(query: String): Flow<List<Note>> = studyDao.searchNotes(query)

    suspend fun insertNote(note: Note): Long = studyDao.insertNote(note)

    suspend fun updateNote(note: Note) = studyDao.updateNote(note)

    suspend fun deleteNote(note: Note) = studyDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = studyDao.deleteNoteById(id)

    suspend fun insertCategory(category: CategoryItem): Long = studyDao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryItem) = studyDao.deleteCategory(category)

    suspend fun deleteCategoryById(id: Long) = studyDao.deleteCategoryById(id)
}
