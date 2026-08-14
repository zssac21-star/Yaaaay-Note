package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppThemeMode
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.StudyBottomNavBar
import com.example.ui.components.StudyTopAppBar
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val themeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()

            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyFlowApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun StudyFlowApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isEditorOpen by viewModel.isEditorOpen.collectAsStateWithLifecycle()
    val editingNote by viewModel.editingNote.collectAsStateWithLifecycle()

    var showNotificationDialog by remember { mutableStateOf(false) }

    if (isEditorOpen) {
        BackHandler {
            viewModel.closeEditor()
        }
        NoteEditorScreen(
            viewModel = viewModel,
            note = editingNote
        )
    } else {
        Scaffold(
            topBar = {
                StudyTopAppBar(
                    onNotificationClick = { showNotificationDialog = true }
                )
            },
            bottomBar = {
                StudyBottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_content_animation"
                ) { targetTab ->
                    when (targetTab) {
                        ScreenTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        ScreenTab.NOTES -> NotesScreen(viewModel = viewModel)
                        ScreenTab.CATEGORIES -> CategoriesScreen(viewModel = viewModel)
                        ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Study Reminders & Updates") },
            text = {
                Text("🔔 Reminder: You have an exam prep session scheduled for 'Neurobiology Ch 4: Synaptic Transmission'. Keep up the great flow!")
            },
            confirmButton = {
                Button(onClick = { showNotificationDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

