package com.hananel.workschedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hananel.workschedule.data.AppDatabase
import com.hananel.workschedule.ui.*
import com.hananel.workschedule.ui.theme.*
import com.hananel.workschedule.viewmodel.ScheduleViewModel
import com.hananel.workschedule.viewmodel.ScheduleViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()

        setContent {
            WorkScheduleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WorkScheduleApp()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "work_schedule_reminders",
                "תזכורות סידור עבודה",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "תזכורות לעריכת סידור העבודה השבועי"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}

@Composable
fun WorkScheduleApp() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    
    // Get context first
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    // Initialize database and ViewModel  
    val database = remember { AppDatabase.getDatabase(context) }
    val viewModel: ScheduleViewModel = viewModel(factory = ScheduleViewModelFactory(database))
    
    // Auto-save draft when app goes to background or is destroyed
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                // Save draft when app goes to background
                viewModel.saveDraftOnAppClose()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Collect state from ViewModel
    val employees by viewModel.employees.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val hasTempDraft by viewModel.hasTempDraft.collectAsState()
    val draftHasManualAssignments by viewModel.draftHasManualAssignments.collectAsState()
    val duplicateDialog by viewModel.duplicateScheduleDialog.collectAsState()
    val templateData by viewModel.activeTemplate.collectAsState()
    
    // Handle system back button — tab screens let the system handle back (app close)
    BackHandler(enabled = !currentScreen.showsBottomNav() && currentScreen != Screen.SPLASH) {
        when (currentScreen) {
            Screen.EMPLOYEE_MANAGEMENT -> currentScreen = Screen.SETTINGS
            Screen.TEMPLATE_SETUP -> currentScreen = Screen.SETTINGS
            Screen.BLOCKING -> currentScreen = Screen.HOME
            Screen.MANUAL_CREATION -> currentScreen = Screen.BLOCKING
            Screen.AUTO_SCHEDULE_REVIEW -> {
                viewModel.discardAutoSchedule()
                currentScreen = Screen.BLOCKING
            }
            Screen.PREVIEW -> {
                viewModel.resetSessionOnReturnHome()
                currentScreen = Screen.HOME
            }
            Screen.LANDSCAPE_BLOCKING -> currentScreen = Screen.BLOCKING
            Screen.LANDSCAPE_MANUAL -> currentScreen = Screen.MANUAL_CREATION
            Screen.LANDSCAPE_PREVIEW -> currentScreen = Screen.PREVIEW
            Screen.LANDSCAPE_AUTO_SCHEDULE_REVIEW -> currentScreen = Screen.AUTO_SCHEDULE_REVIEW
            else -> currentScreen = Screen.HOME
        }
    }
    
    Scaffold(
        bottomBar = {
            if (currentScreen.showsBottomNav()) {
                AppBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {

    when (currentScreen) {
        Screen.SPLASH -> {
            SplashScreen(
                onTimeout = { currentScreen = Screen.HOME }
            )
        }
        
        Screen.HOME -> {
            // Load draft from database if exists, then check for temp draft
            LaunchedEffect(Unit) {
                viewModel.loadDraftOnAppStart() // Load saved draft from DB first
                viewModel.checkTempDraftOnStart() // Then update temp draft status
            }
            
            HomeScreen(
                scheduleCount = schedules.size,
                employeeCount = employees.size,
                hasTemplate = templateData != null, // Pass template status to HomeScreen
                onRecentSchedulesClick = { currentScreen = Screen.HISTORY },
                onNewScheduleClick = { 
                    // Check if template exists - MUST have template before creating schedule
                    if (templateData == null) {
                        // No template - redirect to template setup (required!)
                        currentScreen = Screen.TEMPLATE_SETUP
                    } else {
                        // Has template - start fresh schedule
                        viewModel.startNewSchedule()
                        currentScreen = Screen.BLOCKING
                    }
                },
                onGoToTemplateSetup = {
                    // Direct navigation to template setup from warning message
                    currentScreen = Screen.TEMPLATE_SETUP
                },
                onContinueTempDraftClick = {
                    // Continue existing draft
                    viewModel.continueTempDraft()
                    // Navigate to correct screen based on draft content
                    currentScreen = if (draftHasManualAssignments) {
                        Screen.MANUAL_CREATION // Has manual assignments - go directly to manual screen
                    } else {
                        Screen.BLOCKING // Only blocks - go to blocking screen
                    }
                },
                onEmployeeManagementClick = { currentScreen = Screen.EMPLOYEE_MANAGEMENT },
                onTemplateSetupClick = { currentScreen = Screen.TEMPLATE_SETUP },
                onReminderClick = { currentScreen = Screen.REMINDER_SETTINGS },
                hasTempDraft = hasTempDraft
            )
        }
        
        Screen.EMPLOYEE_MANAGEMENT -> {
            EmployeeManagementScreen(
                employees = employees,
                onAddEmployee = { name, shabbatObserver, isMitgaber -> 
                    viewModel.addEmployee(name, shabbatObserver, isMitgaber)
                },
                onUpdateEmployee = { employee -> 
                    viewModel.updateEmployee(employee)
                },
                onDeleteEmployee = { employee -> 
                    viewModel.deleteEmployee(employee)
                },
                onBackClick = { currentScreen = Screen.SETTINGS }
            )
        }

        Screen.TEMPLATE_SETUP -> {
            val editingShiftRows by viewModel.editingShiftRows.collectAsState()
            val editingDayColumns by viewModel.editingDayColumns.collectAsState()
            val hasExistingTemplate = templateData != null

            // Load template for editing (or load default values if no template)
            LaunchedEffect(Unit) {
                viewModel.loadTemplateForEditing()
            }
            
            ShiftTemplateSetupScreen(
                shiftRows = editingShiftRows,
                dayColumns = editingDayColumns,
                hasExistingTemplate = hasExistingTemplate, // Dynamic title
                onAddShiftRow = { name, hours, note ->
                    viewModel.addShiftRow(name, hours, note)
                },
                onEditShiftRow = { index, name, hours, note ->
                    viewModel.editShiftRow(index, name, hours, note)
                },
                onEditDayColumnNote = { index, note ->
                    viewModel.editDayColumnNote(index, note)
                },
                onDeleteShiftRow = { index ->
                    viewModel.deleteShiftRow(index)
                },
                onMoveShiftRow = { fromIndex, toIndex ->
                    viewModel.moveShiftRow(fromIndex, toIndex)
                },
                onToggleDayColumn = { index ->
                    viewModel.toggleDayColumn(index)
                },
                onAutoSave = {
                    viewModel.saveTemplate() // Just save, don't navigate
                },
                onSaveAndExit = {
                    viewModel.saveTemplate()
                    currentScreen = Screen.SETTINGS
                },
                onBackClick = { currentScreen = Screen.SETTINGS }
            )
        }
        
        Screen.BLOCKING -> {
            val selectedEmployee by viewModel.selectedEmployee.collectAsState()
            val blockingMode by viewModel.blockingMode.collectAsState()
            val blocks by viewModel.blocks.collectAsState()
            val canOnlyBlocks by viewModel.canOnlyBlocks.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val snackbarMessage by viewModel.snackbarMessage.collectAsState()
            val isEditingScheduleBlocks by viewModel.isEditingScheduleBlocks.collectAsState()
            val editedScheduleName by viewModel.editedScheduleName.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()
            val autoGenerationComplete by viewModel.autoGenerationComplete.collectAsState()
            
            // Navigate to review screen when auto-generation completes
            LaunchedEffect(autoGenerationComplete) {
                if (autoGenerationComplete) {
                    currentScreen = Screen.AUTO_SCHEDULE_REVIEW
                    viewModel.resetAutoGenerationFlag()
                }
            }
            
            BlockingScreen(
                employees = employees,
                selectedEmployee = selectedEmployee,
                blockingMode = blockingMode,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                weekStartDate = weekStartDate,
                snackbarMessage = snackbarMessage,
                isEditingScheduleBlocks = isEditingScheduleBlocks,
                editedScheduleName = editedScheduleName,
                templateData = templateData,
                onSelectEmployee = { employee -> viewModel.selectEmployee(employee) },
                onSetBlockingMode = { mode -> viewModel.setBlockingMode(mode) },
                onToggleBlock = { employee, day, shift -> 
                    viewModel.toggleBlock(employee, day, shift)
                },
                onBlockAllShiftsForDay = { employee, day ->
                    viewModel.blockAllShiftsForDay(employee, day)
                },
                onToggleSavingMode = { day -> viewModel.toggleSavingMode(day) },
                onSetWeekStartDate = { date -> viewModel.setWeekStartDate(date) },
                onGenerateManualSchedule = {
                    viewModel.prepareForManualCreation()
                    currentScreen = Screen.MANUAL_CREATION
                },
                onGenerateAutomaticSchedule = {
                    viewModel.generateSchedule() // Use new generic algorithm
                    // Navigation will happen automatically when generation completes
                },
                onReturnToSavedSchedule = {
                    viewModel.returnToSavedScheduleWithUpdatedBlocks()
                    currentScreen = Screen.PREVIEW
                },
                onOverrideAndCreateNew = {
                    viewModel.overrideAndCreateNewManualSchedule()
                    currentScreen = Screen.MANUAL_CREATION
                },
                onCreateScheduleCopy = {
                    viewModel.createScheduleCopy()
                    currentScreen = Screen.MANUAL_CREATION
                },
                onClearAllBlocks = { viewModel.clearAllBlocks() },
                onDismissSnackbar = { viewModel.clearSnackbarMessage() },
                onBackClick = { currentScreen = Screen.HOME },
                onEnterLandscape = { currentScreen = Screen.LANDSCAPE_BLOCKING }
            )
        }
        
        Screen.MANUAL_CREATION -> {
            val selectedEmployee by viewModel.selectedEmployee.collectAsState()
            val currentSchedule by viewModel.currentSchedule.collectAsState()
            val blocks by viewModel.blocks.collectAsState()
            val canOnlyBlocks by viewModel.canOnlyBlocks.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()
            
            var shouldNavigate by remember { mutableStateOf(false) }
            
            // Handle navigation after generateManualSchedule is called
            LaunchedEffect(shouldNavigate, duplicateDialog) {
                if (shouldNavigate && duplicateDialog == null) {
                    kotlinx.coroutines.delay(100) // Small delay to let save complete
                    if (!viewModel.isScheduleEmpty()) {
                        currentScreen = Screen.PREVIEW
                        shouldNavigate = false
                    }
                }
            }
            
            ManualCreationScreen(
                employees = employees,
                selectedEmployee = selectedEmployee,
                schedule = currentSchedule,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                weekStartDate = weekStartDate, // Read-only for display
                templateData = templateData,
                onSelectEmployee = { employee -> viewModel.selectEmployee(employee) },
                onToggleEmployeeInShift = { employee, day, shift ->
                    viewModel.toggleEmployeeInManualSchedule(employee, day, shift)
                },
                onAddFreeTextToCell = { cellKey, text ->
                    viewModel.addFreeTextToCell(cellKey, text)
                },
                onGenerateManualSchedule = {
                    viewModel.generateManualSchedule()
                    shouldNavigate = true // Trigger navigation check
                },
                onReturnToBlocking = {
                    // Simply return to blocking (no saving needed - auto-saved by tempDraft system)
                    currentScreen = Screen.BLOCKING
                },
                onClearManualSchedule = {
                    // Clear only manual assignments, keep blocks intact
                    viewModel.clearManualSchedule()
                },
                onBackClick = {
                    // Simple back navigation (data is auto-saved by tempDraft system)
                    currentScreen = Screen.HOME
                },
                onEnterLandscape = { currentScreen = Screen.LANDSCAPE_MANUAL }
            )
        }
        
        Screen.AUTO_SCHEDULE_REVIEW -> {
            val currentSchedule by viewModel.currentSchedule.collectAsState()
            val blocks by viewModel.blocks.collectAsState()
            val canOnlyBlocks by viewModel.canOnlyBlocks.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()
            val impossibleShifts by viewModel.lastImpossibleShifts.collectAsState()
            val totalShifts by viewModel.totalShiftsInSchedule.collectAsState()
            val autoConfirmComplete by viewModel.autoConfirmComplete.collectAsState()

            // Navigate to PREVIEW only after confirmAutoSchedule() completes (incl. duplicate resolution)
            LaunchedEffect(autoConfirmComplete) {
                if (autoConfirmComplete) {
                    currentScreen = Screen.PREVIEW
                    viewModel.resetAutoConfirmFlag()
                }
            }

            AutoScheduleReviewScreen(
                employees = employees,
                schedule = currentSchedule,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                weekStartDate = weekStartDate,
                templateData = templateData,
                impossibleShifts = impossibleShifts,
                totalShifts = totalShifts,
                onConfirm = { viewModel.confirmAutoSchedule() },
                onBackToBlocking = {
                    viewModel.discardAutoSchedule()
                    currentScreen = Screen.BLOCKING
                },
                onEnterLandscape = { currentScreen = Screen.LANDSCAPE_AUTO_SCHEDULE_REVIEW }
            )
        }

        Screen.PREVIEW -> {
            val currentSchedule by viewModel.currentSchedule.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val isEditingExistingSchedule by viewModel.isEditingExistingSchedule.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()
            
            // Clear draft only when it's a NEW schedule reaching preview.
            // When viewing from History (isEditingExistingSchedule=true), do NOT touch the draft -
            // the user might have a real in-progress draft in DB that must not be deleted.
            LaunchedEffect(isEditingExistingSchedule) {
                if (!isEditingExistingSchedule) {
                    viewModel.clearDraft()
                }
            }
            
            PreviewScreen(
                employees = employees,
                schedule = currentSchedule,
                errorMessage = errorMessage,
                savingMode = savingMode,
                weekStartDate = weekStartDate,
                templateData = templateData,
                onUpdateCell = { key, value -> 
                    viewModel.updateScheduleCell(key, value)
                },
                onSaveSchedule = { 
                    // Deprecated - smart save handles this automatically
                    // Keep for compatibility but don't use
                },
                onShareSchedule = { shareType ->
                    try {
                        val weekStartString = weekStartDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val bitmap = com.hananel.workschedule.utils.ImageSharer.generateScheduleImage(
                            context, currentSchedule, savingMode, weekStartString, templateData
                        )
                        when (shareType) {
                            ShareType.WHATSAPP_IMAGE ->
                                com.hananel.workschedule.utils.ImageSharer.shareScheduleImage(context, bitmap)
                            ShareType.DOWNLOAD_IMAGE ->
                                com.hananel.workschedule.utils.ImageSharer.saveScheduleImageToGallery(context, bitmap)
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "שגיאה ביצירת תמונה", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onBackClick = {
                    viewModel.resetSessionOnReturnHome()
                    currentScreen = Screen.HOME
                },
                onReturnToBlocking = {
                    viewModel.navigateToBlocksEditingFromPreview()
                    currentScreen = Screen.BLOCKING
                },
                onDismissError = {
                    viewModel.clearErrorMessage()
                },
                onEnterLandscape = { currentScreen = Screen.LANDSCAPE_PREVIEW },
                onUpdateShiftNote = { shiftName, note ->
                    viewModel.updateScheduleShiftNote(shiftName, note)
                },
                onUpdateDayNote = { dayName, note ->
                    viewModel.updateScheduleDayNote(dayName, note)
                },
                isEditingExistingSchedule = isEditingExistingSchedule
            )
        }
        
        Screen.HISTORY -> {
            HistoryScreen(
                schedules = schedules,
                onScheduleClick = { schedule ->
                    // Load selected schedule and go to preview
                    viewModel.loadSchedule(schedule)
                    currentScreen = Screen.PREVIEW
                },
                onDeleteSchedule = { schedule ->
                    viewModel.deleteSchedule(schedule)
                },
                onRenameSchedule = { schedule, newName ->
                    viewModel.updateScheduleName(schedule, newName)
                },
                onBackClick = { currentScreen = Screen.HOME }
            )
        }

        Screen.REMINDER_SETTINGS -> {
            ReminderScreen(
                onBackClick = { currentScreen = Screen.HOME }
            )
        }

        Screen.SETTINGS -> {
            SettingsScreen(
                employeeCount = employees.size,
                appVersion = "2.1",
                onNavigateToEmployees = { currentScreen = Screen.EMPLOYEE_MANAGEMENT },
                onNavigateToTemplate = { currentScreen = Screen.TEMPLATE_SETUP },
                onNavigateToReminders = { currentScreen = Screen.REMINDER_SETTINGS }
            )
        }

        Screen.LANDSCAPE_BLOCKING -> {
            val selectedEmployee by viewModel.selectedEmployee.collectAsState()
            val blockingMode by viewModel.blockingMode.collectAsState()
            val blocks by viewModel.blocks.collectAsState()
            val canOnlyBlocks by viewModel.canOnlyBlocks.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()

            LandscapeBlockingScreen(
                employees = employees,
                selectedEmployee = selectedEmployee,
                blockingMode = blockingMode,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                weekStartDate = weekStartDate,
                templateData = templateData,
                onSelectEmployee = { employee -> viewModel.selectEmployee(employee) },
                onSetBlockingMode = { mode -> viewModel.setBlockingMode(mode) },
                onToggleBlock = { employee, day, shift ->
                    viewModel.toggleBlock(employee, day, shift)
                },
                onBlockAllShiftsForDay = { employee, day ->
                    viewModel.blockAllShiftsForDay(employee, day)
                },
                onSetWeekStartDate = { date -> viewModel.setWeekStartDate(date) },
                onClose = { currentScreen = Screen.BLOCKING }
            )
        }

        Screen.LANDSCAPE_MANUAL -> {
            val selectedEmployee by viewModel.selectedEmployee.collectAsState()
            val currentSchedule by viewModel.currentSchedule.collectAsState()
            val blocks by viewModel.blocks.collectAsState()
            val canOnlyBlocks by viewModel.canOnlyBlocks.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()

            LandscapeManualScreen(
                employees = employees,
                selectedEmployee = selectedEmployee,
                schedule = currentSchedule,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                weekStartDate = weekStartDate,
                templateData = templateData,
                onSelectEmployee = { employee -> viewModel.selectEmployee(employee) },
                onToggleEmployeeInShift = { employee, day, shift ->
                    viewModel.toggleEmployeeInManualSchedule(employee, day, shift)
                },
                onFreeTextToCell = { key, value -> viewModel.updateScheduleCell(key, value) },
                onClose = { currentScreen = Screen.MANUAL_CREATION }
            )
        }

        Screen.LANDSCAPE_PREVIEW -> {
            val currentSchedule by viewModel.currentSchedule.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()

            LandscapePreviewScreen(
                employees = employees,
                schedule = currentSchedule,
                savingMode = savingMode,
                weekStartDate = weekStartDate,
                templateData = templateData,
                onUpdateCell = { key, value ->
                    viewModel.updateScheduleCell(key, value)
                },
                onShareSchedule = { shareType ->
                    try {
                        val weekStartString = weekStartDate.format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        )
                        val bitmap = com.hananel.workschedule.utils.ImageSharer.generateScheduleImage(
                            context, currentSchedule, savingMode, weekStartString, templateData
                        )
                        when (shareType) {
                            ShareType.WHATSAPP_IMAGE ->
                                com.hananel.workschedule.utils.ImageSharer.shareScheduleImage(context, bitmap)
                            ShareType.DOWNLOAD_IMAGE ->
                                com.hananel.workschedule.utils.ImageSharer.saveScheduleImageToGallery(context, bitmap)
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "שגיאה ביצירת תמונה", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onReturnToBlocking = {
                    viewModel.navigateToBlocksEditingFromPreview()
                    currentScreen = Screen.BLOCKING
                },
                onClose = { currentScreen = Screen.PREVIEW }
            )
        }

        Screen.LANDSCAPE_AUTO_SCHEDULE_REVIEW -> {
            val currentSchedule by viewModel.currentSchedule.collectAsState()
            val blocks by viewModel.blocks.collectAsState()
            val canOnlyBlocks by viewModel.canOnlyBlocks.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()
            val impossibleShifts by viewModel.lastImpossibleShifts.collectAsState()
            val autoConfirmComplete by viewModel.autoConfirmComplete.collectAsState()

            LaunchedEffect(autoConfirmComplete) {
                if (autoConfirmComplete) {
                    currentScreen = Screen.PREVIEW
                    viewModel.resetAutoConfirmFlag()
                }
            }

            LandscapeAutoScheduleReviewScreen(
                employees = employees,
                schedule = currentSchedule,
                blocks = blocks,
                canOnlyBlocks = canOnlyBlocks,
                savingMode = savingMode,
                weekStartDate = weekStartDate,
                templateData = templateData,
                impossibleShifts = impossibleShifts,
                onConfirm = { viewModel.confirmAutoSchedule() },
                onBackToBlocking = {
                    viewModel.discardAutoSchedule()
                    currentScreen = Screen.BLOCKING
                },
                onClose = { currentScreen = Screen.AUTO_SCHEDULE_REVIEW }
            )
        }
    } // end when(currentScreen)
    } // end Box
    } // end Scaffold

    // Unified Duplicate Schedule Dialog
    duplicateDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { viewModel.onDuplicateDialogDismiss() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Orange
                )
            },
            title = {
                Text(
                    text = "קיים כבר סידור עבודה לשבוע הזה בהיסטוריה",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "האם ברצונך:",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            viewModel.onDuplicateDialogOverwrite()
                            currentScreen = Screen.PREVIEW
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlockedRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("לדרוס את הקיים", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.onDuplicateDialogCreateNew()
                            currentScreen = Screen.PREVIEW
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ליצור עותק חדש (${dialog.existingCount})", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {}
        )
    }
}

enum class Screen {
    SPLASH, HOME, EMPLOYEE_MANAGEMENT, TEMPLATE_SETUP, BLOCKING, MANUAL_CREATION,
    AUTO_SCHEDULE_REVIEW, PREVIEW, HISTORY, REMINDER_SETTINGS, SETTINGS,
    LANDSCAPE_BLOCKING, LANDSCAPE_MANUAL, LANDSCAPE_PREVIEW, LANDSCAPE_AUTO_SCHEDULE_REVIEW
}

/** Tab screens show the bottom navigation bar; workflow/detail screens hide it. */
fun Screen.showsBottomNav(): Boolean = this in setOf(
    Screen.HOME, Screen.HISTORY, Screen.REMINDER_SETTINGS, Screen.SETTINGS
)

@Composable
fun AppBottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("בית") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.HISTORY,
            onClick = { onNavigate(Screen.HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("סידורים") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.REMINDER_SETTINGS,
            onClick = { onNavigate(Screen.REMINDER_SETTINGS) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            label = { Text("תזכורות") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.SETTINGS,
            onClick = { onNavigate(Screen.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("הגדרות") }
        )
    }
}