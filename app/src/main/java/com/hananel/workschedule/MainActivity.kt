package com.hananel.workschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    
    // Handle system back button - natural navigation like other Android apps
    BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.SPLASH) {
        currentScreen = when (currentScreen) {
            Screen.EMPLOYEE_MANAGEMENT -> Screen.HOME
            Screen.TEMPLATE_SETUP -> Screen.HOME
            Screen.BLOCKING -> Screen.HOME
            Screen.MANUAL_CREATION -> Screen.BLOCKING // Manual creation goes back to blocking
            Screen.PREVIEW -> Screen.HOME
            Screen.HISTORY -> Screen.HOME
            else -> Screen.HOME // fallback
        }
    }
    
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
                onBackClick = { currentScreen = Screen.HOME }
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
                onAddShiftRow = { name, hours ->
                    viewModel.addShiftRow(name, hours) // New: requires name and hours
                },
                onEditShiftRow = { index, name, hours ->
                    viewModel.editShiftRow(index, name, hours)
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
                    currentScreen = Screen.HOME // Save AND navigate
                },
                onBackClick = { currentScreen = Screen.HOME }
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
            val templateData by viewModel.activeTemplate.collectAsState()
            
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
                onReturnToSavedSchedule = {
                    viewModel.returnToSavedScheduleWithUpdatedBlocks()
                    currentScreen = Screen.PREVIEW
                },
                onOverrideAndCreateNew = {
                    viewModel.overrideAndCreateNewManualSchedule()
                    currentScreen = Screen.MANUAL_CREATION
                },
                onClearAllBlocks = { viewModel.clearAllBlocks() },
                onDismissSnackbar = { viewModel.clearSnackbarMessage() },
                onBackClick = { currentScreen = Screen.HOME }
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
                }
            )
        }
        
        Screen.PREVIEW -> {
            val currentSchedule by viewModel.currentSchedule.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState()
            val savingMode by viewModel.savingMode.collectAsState()
            val weekStartDate by viewModel.weekStartDate.collectAsState()
            val isEditingExistingSchedule by viewModel.isEditingExistingSchedule.collectAsState()
            val templateData by viewModel.activeTemplate.collectAsState()
            
            // Clear draft completely when reaching preview - schedule is already saved to history
            LaunchedEffect(Unit) {
                viewModel.clearDraft()
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
                    when (shareType) {
                        ShareType.WHATSAPP_IMAGE -> {
                            // Generate and share schedule image via WhatsApp with correct date
                            val weekStartString = weekStartDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val bitmap = com.hananel.workschedule.utils.ImageSharer.generateScheduleImage(
                                context,
                                currentSchedule,
                                savingMode,
                                weekStartString,
                                templateData
                            )
                            com.hananel.workschedule.utils.ImageSharer.shareScheduleImage(
                                context,
                                bitmap
                            )
                        }
                        ShareType.DOWNLOAD_IMAGE -> {
                            // Generate and save schedule image to gallery with correct date
                            val weekStartString = weekStartDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val bitmap = com.hananel.workschedule.utils.ImageSharer.generateScheduleImage(
                                context,
                                currentSchedule,
                                savingMode,
                                weekStartString,
                                templateData
                            )
                            com.hananel.workschedule.utils.ImageSharer.saveScheduleImageToGallery(
                                context,
                                bitmap
                            )
                        }
                    }
                },
                onBackClick = { currentScreen = Screen.HOME },
                onReturnToBlocking = { 
                    // Navigate to blocking - enable editing mode if this is existing schedule
                    viewModel.navigateToBlocksEditingFromPreview()
                    currentScreen = Screen.BLOCKING 
                },
                onDismissError = {
                    viewModel.clearErrorMessage()
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
    }
    
    // Handle automatic navigation to preview after duplicate dialog closes
    LaunchedEffect(duplicateDialog) {
        // Only navigate if dialog was just dismissed (changed from non-null to null)
        // This handles the case where user chose "overwrite" or "create new" in duplicate dialog
        if (duplicateDialog == null && 
            currentScreen == Screen.MANUAL_CREATION) {
            // Dialog was dismissed, check if we're ready to navigate
            // (this will only happen after the dialog actions, not on every schedule change)
        }
    }
    
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
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
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
    SPLASH, HOME, EMPLOYEE_MANAGEMENT, TEMPLATE_SETUP, BLOCKING, MANUAL_CREATION, PREVIEW, HISTORY
}