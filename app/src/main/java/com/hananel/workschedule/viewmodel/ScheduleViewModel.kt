package com.hananel.workschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hananel.workschedule.data.AppDatabase
import com.hananel.workschedule.data.DayColumn
import com.hananel.workschedule.data.Employee
import com.hananel.workschedule.data.Schedule
import com.hananel.workschedule.data.ShiftDefinitions
import com.hananel.workschedule.data.ShiftRow
import com.hananel.workschedule.utils.GenericScheduleGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val database: AppDatabase
) : ViewModel() {
    
    private val employeeDao = database.employeeDao()
    private val scheduleDao = database.scheduleDao()
    private val shiftTemplateDao = database.shiftTemplateDao()
    private val dynamicShiftManager = com.hananel.workschedule.data.DynamicShiftManager(shiftTemplateDao)
    
    // Employee-related state
    val employees = employeeDao.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Template-related state
    val activeTemplate = dynamicShiftManager.getActiveTemplateWithData()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    private val _editingShiftRows = MutableStateFlow<List<com.hananel.workschedule.data.ShiftRow>>(emptyList())
    val editingShiftRows = _editingShiftRows.asStateFlow()
    
    private val _editingDayColumns = MutableStateFlow<List<com.hananel.workschedule.data.DayColumn>>(emptyList())
    val editingDayColumns = _editingDayColumns.asStateFlow()
    
    private val _hasActiveTemplate = MutableStateFlow(false)
    val hasActiveTemplate = _hasActiveTemplate.asStateFlow()
    
    // Schedule-related state
    val schedules = scheduleDao.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // UI state for blocking screen
    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee = _selectedEmployee.asStateFlow()
    
    private val _blockingMode = MutableStateFlow<BlockingMode>(BlockingMode.CANNOT)
    val blockingMode = _blockingMode.asStateFlow()
    
    private val _blocks = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val blocks = _blocks.asStateFlow()
    
    private val _canOnlyBlocks = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val canOnlyBlocks = _canOnlyBlocks.asStateFlow()
    
    private val _savingMode = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val savingMode = _savingMode.asStateFlow()
    
    private val _currentSchedule = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val currentSchedule = _currentSchedule.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String>("")
    val errorMessage = _errorMessage.asStateFlow()
    
    // Snackbar message for blocking screen (non-blocking, dismissible)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage = _snackbarMessage.asStateFlow()
    
    private val _weekStartDate = MutableStateFlow(getNextSunday())
    val weekStartDate = _weekStartDate.asStateFlow()
    
    // State for temp draft detection
    private val _hasTempDraft = MutableStateFlow(false)
    val hasTempDraft = _hasTempDraft.asStateFlow()
    
    // Smart save system - track schedule source for intelligent saving
    private val _currentScheduleId = MutableStateFlow<Int?>(null) // null = new schedule, non-null = editing existing
    val currentScheduleId = _currentScheduleId.asStateFlow()
    
    private val _isEditingExistingSchedule = MutableStateFlow(false)
    val isEditingExistingSchedule = _isEditingExistingSchedule.asStateFlow()
    
    // Track if we're editing blocks of an existing schedule (from history)
    private val _isEditingScheduleBlocks = MutableStateFlow(false)
    val isEditingScheduleBlocks = _isEditingScheduleBlocks.asStateFlow()
    
    // Track the edited schedule's name/date for display in blocking screen title
    private val _editedScheduleName = MutableStateFlow<String?>(null)
    val editedScheduleName = _editedScheduleName.asStateFlow()
    
    // Track if draft has manual assignments (to open correct screen)
    private val _draftHasManualAssignments = MutableStateFlow(false)
    val draftHasManualAssignments = _draftHasManualAssignments.asStateFlow()
    
    // Flag to trigger navigation after auto-generation completes
    private val _autoGenerationComplete = MutableStateFlow(false)
    val autoGenerationComplete = _autoGenerationComplete.asStateFlow()
    
    // Unified duplicate handling system
    private val _duplicateScheduleDialog = MutableStateFlow<DuplicateDialogState?>(null)
    val duplicateScheduleDialog = _duplicateScheduleDialog.asStateFlow()
    
    data class DuplicateDialogState(
        val originalName: String,
        val existingCount: Int,
        val pendingScheduleData: Map<String, List<String>>,
        val isFromManualCreation: Boolean
    )
    
    // Helper function to find next Sunday (or today if it's Sunday)
    private fun getNextSunday(): java.time.LocalDate {
        val today = java.time.LocalDate.now()
        val dayOfWeek = today.dayOfWeek.value // Monday=1, Sunday=7
        return if (dayOfWeek == 7) {
            today // Today is Sunday
        } else {
            today.plusDays((7 - dayOfWeek).toLong()) // Days until next Sunday
        }
    }
    
    // Draft state removed - only using tempDraft system now
    
    // Employee operations
    fun addEmployee(name: String, shabbatObserver: Boolean, isMitgaber: Boolean = false) {
        viewModelScope.launch {
            val employee = Employee(name = name, shabbatObserver = shabbatObserver, isMitgaber = isMitgaber)
            employeeDao.insertEmployee(employee)
            
            // Apply Shabbat blocks if needed
            if (shabbatObserver) {
                applyShabbatObserverBlocks(employee)
            }
        }
    }
    
    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            val wasShabbatObserver = employees.value.find { it.id == employee.id }?.shabbatObserver ?: false
            employeeDao.updateEmployee(employee)
            
            // Handle Shabbat Observer status change
            if (employee.shabbatObserver && !wasShabbatObserver) {
                // Became Shabbat Observer - add blocks
                applyShabbatObserverBlocks(employee)
            } else if (!employee.shabbatObserver && wasShabbatObserver) {
                // No longer Shabbat Observer - remove blocks
                removeShabbatObserverBlocks(employee)
            }
        }
    }
    
    private fun applyShabbatObserverBlocks(employee: Employee) {
        val newBlocks = _blocks.value.toMutableMap()
        
        // Apply Shabbat Observer blocks according to specification
        ShiftDefinitions.shabbatBlockedShifts.forEach { shiftKey ->
            val blockKey = "${employee.name}-$shiftKey"
            newBlocks[blockKey] = true
            
            // Remove from can-only if exists
            val newCanOnly = _canOnlyBlocks.value.toMutableMap()
            newCanOnly.remove(blockKey)
            _canOnlyBlocks.value = newCanOnly
        }
        
        _blocks.value = newBlocks
    }
    
    private fun removeShabbatObserverBlocks(employee: Employee) {
        val newBlocks = _blocks.value.toMutableMap()
        
        // Remove Shabbat Observer blocks
        ShiftDefinitions.shabbatBlockedShifts.forEach { shiftKey ->
            val blockKey = "${employee.name}-$shiftKey"
            newBlocks.remove(blockKey)
        }
        
        _blocks.value = newBlocks
    }
    
    /**
     * Initialize blocking session - apply automatic Shabbat Observer blocks
     * This should be called when entering the blocking screen
     */
    fun initializeBlockingSession() {
        val currentEmployees = employees.value
        val newBlocks = _blocks.value.toMutableMap()
        
        // Apply automatic Shabbat Observer blocks for all Shabbat observers
        currentEmployees.filter { it.shabbatObserver }.forEach { employee ->
            ShiftDefinitions.shabbatBlockedShifts.forEach { shiftKey ->
                val blockKey = "${employee.name}-$shiftKey"
                newBlocks[blockKey] = true
            }
        }
        
        _blocks.value = newBlocks
    }
    
    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            employeeDao.deleteEmployee(employee)
        }
    }
    
    // Blocking operations
    fun selectEmployee(employee: Employee?) {
        _selectedEmployee.value = employee
    }
    
    fun setBlockingMode(mode: BlockingMode) {
        _blockingMode.value = mode
    }
    
    fun toggleBlock(employee: Employee, day: String, shift: String) {
        val key = "${employee.name}-$day-$shift"
        val shiftKey = "$day-$shift"
        
        // Check if this is an automatic Shabbat Observer block that cannot be removed
        val isAutomaticShabbatBlock = employee.shabbatObserver && 
                ShiftDefinitions.shabbatBlockedShifts.contains(shiftKey)
        
        when (_blockingMode.value) {
            BlockingMode.CANNOT -> {
                // PROTECTION: Check if employee already has CAN_ONLY restrictions
                val hasCanOnlyRestrictions = _canOnlyBlocks.value.any { (canOnlyKey, isCanOnly) ->
                    isCanOnly && canOnlyKey.startsWith("${employee.name}-")
                }
                
                val newBlocks = _blocks.value.toMutableMap()
                if (newBlocks[key] == true) {
                    // Employee is already blocked
                    if (isAutomaticShabbatBlock) {
                        // Cannot remove automatic Shabbat Observer blocks
                        return
                    } else {
                        // REMOVE the block (toggle off)
                        newBlocks.remove(key)
                    }
                } else {
                    // Employee is not blocked - ADD the block (toggle on)
                    
                    // Allow only if no CAN_ONLY restrictions exist OR this is an automatic Shabbat block
                    if (hasCanOnlyRestrictions && !isAutomaticShabbatBlock) {
                        // Show snackbar - cannot mix CANNOT and CAN_ONLY for same employee
                        _snackbarMessage.value = "⚠️ עבור ${employee.name} כבר נבחרו תאים ב'יכול רק'.\nלא ניתן לשלב גם 'לא יכול' לאותו עובד."
                        return
                    }
                    
                    newBlocks[key] = true
                    // Remove from can-only if exists (can't be both for same shift)
                    val newCanOnly = _canOnlyBlocks.value.toMutableMap()
                    newCanOnly.remove(key)
                    _canOnlyBlocks.value = newCanOnly
                }
                _blocks.value = newBlocks
            }
            
            BlockingMode.CAN_ONLY -> {
                // Shabbat Observers cannot set "CAN ONLY" on their automatically blocked shifts
                if (isAutomaticShabbatBlock) {
                    return
                }
                
                // PROTECTION: Check if employee already has manual CANNOT restrictions (except automatic Shabbat blocks)
                val hasManualCannotRestrictions = _blocks.value.any { (blockKey, isBlocked) ->
                    if (!isBlocked) return@any false
                    
                    val parts = blockKey.split("-")
                    if (parts.size < 3 || parts[0] != employee.name) return@any false
                    
                    val blockDay = parts[1]
                    val blockShift = parts[2]
                    val blockShiftKey = "$blockDay-$blockShift"
                    
                    // This is a manual block (not automatic Shabbat block)
                    !(employee.shabbatObserver && ShiftDefinitions.shabbatBlockedShifts.contains(blockShiftKey))
                }
                
                val newCanOnly = _canOnlyBlocks.value.toMutableMap()
                if (newCanOnly[key] == true) {
                    // Employee is already in can-only - REMOVE it (toggle off)
                    newCanOnly.remove(key)
                } else {
                    // Employee is not in can-only - ADD it (toggle on)
                    
                    if (hasManualCannotRestrictions) {
                        // Show snackbar - cannot mix CANNOT and CAN_ONLY for same employee
                        _snackbarMessage.value = "⚠️ עבור ${employee.name} כבר נבחרו תאים ב'לא יכול'.\nלא ניתן לשלב גם 'יכול רק' לאותו עובד."
                        return
                    }
                    
                    newCanOnly[key] = true
                    // Remove from blocks if exists (can't be both) - but not automatic ones  
                    if (!isAutomaticShabbatBlock) {
                        val newBlocks = _blocks.value.toMutableMap()
                        newBlocks.remove(key)
                        _blocks.value = newBlocks
                    }
                }
                _canOnlyBlocks.value = newCanOnly
            }
        }
        
        // Smart save/draft: if editing existing schedule, save directly; otherwise mark as draft
        if (_isEditingScheduleBlocks.value && _currentScheduleId.value != null) {
            // Editing existing schedule - save changes directly to history
            saveScheduleChanges()
        } else {
            // New schedule - mark as temp draft
            updateTempDraftStatus()
        }
    }
    
    fun toggleSavingMode(day: String) {
        val newSavingMode = _savingMode.value.toMutableMap()
        newSavingMode[day] = !(newSavingMode[day] ?: false)
        _savingMode.value = newSavingMode
    }
    
    fun setWeekStartDate(date: java.time.LocalDate) {
        _weekStartDate.value = date
    }
    
    fun toggleEmployeeInManualSchedule(employee: Employee, day: String, shift: String) {
        val key = "$day-$shift"
        val currentSchedule = _currentSchedule.value.toMutableMap()
        val currentEmployees = currentSchedule[key]?.toMutableList() ?: mutableListOf()
        
        if (currentEmployees.contains(employee.name)) {
            // Remove employee from this shift
            currentEmployees.remove(employee.name)
        } else {
            // Add employee to this shift
            currentEmployees.add(employee.name)
        }
        
        currentSchedule[key] = currentEmployees
        _currentSchedule.value = currentSchedule
        updateTempDraftStatus()
    }
    
    fun generateManualSchedule() {
        // Manual schedule is already built by user interactions
        // Use unified duplicate checking system
        viewModelScope.launch {
            if (!isScheduleEmpty()) {
                // NEW: Don't convert can-only to cannot - keep them as is!
                // User will see blue "יכול" cells and red "לא יכול" cells in manual screen
                
                val weekStart = getScheduleWeekStart()
                val duplicates = checkDuplicateScheduleName(weekStart)
                
                if (duplicates.isNotEmpty()) {
                    // Show unified duplicate dialog
                    _duplicateScheduleDialog.value = DuplicateDialogState(
                        originalName = weekStart,
                        existingCount = duplicates.size,
                        pendingScheduleData = _currentSchedule.value,
                        isFromManualCreation = true
                    )
                } else {
                    // No duplicates - save directly
                    saveSchedule(weekStart)
                    finishScheduleCreation()
                }
            } else {
                // Empty schedule - just clear temp draft
                _hasTempDraft.value = false
            }
        }
    }
    
    fun addFreeTextToCell(cellKey: String, text: String) {
        val currentSchedule = _currentSchedule.value.toMutableMap()
        // Replace the content with free text
        currentSchedule[cellKey] = if (text.isNotEmpty()) listOf(text) else emptyList()
        _currentSchedule.value = currentSchedule
        updateTempDraftStatus()
    }
    
    fun cancelAutomaticSchedule() {
        // Cancel automatic schedule generation - clear the generated schedule
        _currentSchedule.value = emptyMap()
        _errorMessage.value = ""
        updateTempDraftStatus()
    }
    
    // Simplified temp draft management - no complex dialogs
    fun clearTempDraft() {
        // Clear temp draft status when reaching preview (data already saved)
        _hasTempDraft.value = false
        _draftHasManualAssignments.value = false
    }
    
    fun clearAllBlocks() {
        // Clear all manual blocks (keep automatic Shabbat observer blocks)
        val currentEmployees = employees.value
        val newBlocks = mutableMapOf<String, Boolean>()
        val newCanOnlyBlocks = mutableMapOf<String, Boolean>()

        // Re-apply only automatic Shabbat Observer blocks
        currentEmployees.filter { it.shabbatObserver }.forEach { employee ->
            ShiftDefinitions.shabbatBlockedShifts.forEach { shiftKey ->
                val blockKey = "${employee.name}-$shiftKey"
                newBlocks[blockKey] = true
            }
        }

        _blocks.value = newBlocks
        _canOnlyBlocks.value = newCanOnlyBlocks
        _selectedEmployee.value = null
        
        // Reset editing states when clearing blocks
        _isEditingScheduleBlocks.value = false
    }
    
    fun blockAllShiftsForDay(employee: Employee, day: String) {
        // Get all shifts for the specified day from template or fallback to defaults
        val templateData = activeTemplate.value // Use the StateFlow value directly
        val shifts = if (templateData != null) {
            // Use dynamic template - get all shift names for this day
            templateData.shiftRows.map { it.shiftName }
        } else {
            // Fallback to hardcoded shifts
            val isSaving = _savingMode.value[day] ?: false
            ShiftDefinitions.getShiftsForDay(day, isSaving).map { it.id }
        }
        
        // CHECK FOR TOGGLE: See if employee is already in ALL cells of this column
        val currentBlocks = _blocks.value
        val currentCanOnly = _canOnlyBlocks.value
        
        val employeeKeysInColumn = shifts.map { shift -> "${employee.name}-$day-$shift" }
        val allCellsHaveEmployee = when (_blockingMode.value) {
            BlockingMode.CANNOT -> {
                employeeKeysInColumn.all { key -> currentBlocks[key] == true }
            }
            BlockingMode.CAN_ONLY -> {
                employeeKeysInColumn.all { key -> currentCanOnly[key] == true }
            }
        }
        
        // If employee is in all cells, REMOVE (toggle off). Otherwise, ADD (toggle on)
        if (allCellsHaveEmployee) {
            // TOGGLE OFF - Remove employee from all cells in this column
            when (_blockingMode.value) {
                BlockingMode.CANNOT -> {
                    val newBlocks = _blocks.value.toMutableMap()
                    employeeKeysInColumn.forEach { key ->
                        // Check if this is an automatic Shabbat block - don't remove those
                        val parts = key.split("-")
                        if (parts.size >= 3) {
                            val shiftKey = "${parts[1]}-${parts[2]}"
                            val isAutomaticShabbatBlock = employee.shabbatObserver &&
                                    ShiftDefinitions.shabbatBlockedShifts.contains(shiftKey)
                            if (!isAutomaticShabbatBlock) {
                                newBlocks.remove(key)
                            }
                        }
                    }
                    _blocks.value = newBlocks
                }
                BlockingMode.CAN_ONLY -> {
                    val newCanOnly = _canOnlyBlocks.value.toMutableMap()
                    employeeKeysInColumn.forEach { key ->
                        newCanOnly.remove(key)
                    }
                    _canOnlyBlocks.value = newCanOnly
                }
            }
        } else {
            // TOGGLE ON - Add employee to all cells in this column
            when (_blockingMode.value) {
                BlockingMode.CANNOT -> {
                    // PROTECTION: Check if employee already has CAN_ONLY restrictions
                    val hasCanOnlyRestrictions = _canOnlyBlocks.value.any { (canOnlyKey, isCanOnly) ->
                        isCanOnly && canOnlyKey.startsWith("${employee.name}-")
                    }
                    
                    if (hasCanOnlyRestrictions) {
                        // Show snackbar - cannot mix CANNOT and CAN_ONLY for same employee
                        _snackbarMessage.value = "⚠️ עבור ${employee.name} כבר נבחרו תאים ב'יכול רק'.\nלא ניתן לשלב גם 'לא יכול' לאותו עובד."
                        return
                    }
                    
                    val newBlocks = _blocks.value.toMutableMap()
                    shifts.forEach { shift ->
                        val key = "${employee.name}-$day-$shift"
                        newBlocks[key] = true
                        // Remove from can-only if exists
                        val newCanOnly = _canOnlyBlocks.value.toMutableMap()
                        newCanOnly.remove(key)
                        _canOnlyBlocks.value = newCanOnly
                    }
                    _blocks.value = newBlocks
                }
                
                BlockingMode.CAN_ONLY -> {
                    // PROTECTION: Check if employee already has manual CANNOT restrictions (except automatic Shabbat blocks)
                    val hasManualCannotRestrictions = _blocks.value.any { (blockKey, isBlocked) ->
                        if (!isBlocked) return@any false
                        
                        val parts = blockKey.split("-")
                        if (parts.size < 3 || parts[0] != employee.name) return@any false
                        
                        val blockDay = parts[1]
                        val blockShift = parts[2]
                        val blockShiftKey = "$blockDay-$blockShift"
                        
                        // This is a manual block (not automatic Shabbat block)
                        !(employee.shabbatObserver && ShiftDefinitions.shabbatBlockedShifts.contains(blockShiftKey))
                    }
                    
                    if (hasManualCannotRestrictions) {
                        // Show snackbar - cannot mix CANNOT and CAN_ONLY for same employee
                        _snackbarMessage.value = "⚠️ עבור ${employee.name} כבר נבחרו תאים ב'לא יכול'.\nלא ניתן לשלב גם 'יכול רק' לאותו עובד."
                        return
                    }
                    
                    val newBlocks = _blocks.value.toMutableMap()
                    val newCanOnly = _canOnlyBlocks.value.toMutableMap()
                    
                    shifts.forEach { shift ->
                        val key = "${employee.name}-$day-$shift"
                        newCanOnly[key] = true
                        // Remove from blocks if exists (but not automatic Shabbat ones)
                        val isAutomaticShabbatBlock = employee.shabbatObserver &&
                                ShiftDefinitions.shabbatBlockedShifts.contains("$day-$shift")
                        if (!isAutomaticShabbatBlock) {
                            newBlocks.remove(key)
                        }
                    }
                    
                    _blocks.value = newBlocks
                    _canOnlyBlocks.value = newCanOnly
                }
            }
        }
        
        updateTempDraftStatus()
    }
    
    fun clearManualSchedule() {
        // Clear only the manual schedule assignments, keep blocks intact
        _currentSchedule.value = emptyMap()
        _selectedEmployee.value = null
        updateTempDraftStatus()
    }
    
    // Schedule operations
    fun generateSchedule() {
        viewModelScope.launch {
            val currentEmployees = employees.value
            
            // Apply can-only to cannot conversion first
            val convertedBlocks = convertCanOnlyToCannotBlocks()
            _blocks.value = convertedBlocks
            
            val currentCanOnly = canOnlyBlocks.value
            val currentSaving = savingMode.value
            
            // Get active template
            val templateData = dynamicShiftManager.getActiveTemplateDataSync()
            
            // Apply Shabbat Observer blocks automatically
            val allBlocks = convertedBlocks.toMutableMap()
            if (templateData != null) {
                // Dynamic: block Friday and Saturday for Shabbat observers
                currentEmployees.filter { it.shabbatObserver }.forEach { employee ->
                    templateData.dayColumns.forEach { dayColumn ->
                        if (dayColumn.dayNameHebrew.contains("שישי") || dayColumn.dayNameHebrew.contains("שבת")) {
                            templateData.shiftRows.forEach { shiftRow ->
                                val blockKey = "${employee.name}-${dayColumn.dayNameHebrew}-${shiftRow.shiftName}"
                                allBlocks[blockKey] = true
                            }
                        }
                    }
                }
            } else {
                // Legacy: use hardcoded Shabbat blocked shifts
                currentEmployees.filter { it.shabbatObserver }.forEach { employee ->
                    ShiftDefinitions.shabbatBlockedShifts.forEach { shiftKey ->
                        val blockKey = "${employee.name}-$shiftKey"
                        allBlocks[blockKey] = true
                    }
                }
            }
            
            // Generate schedule using NEW generic algorithm
            if (templateData == null) {
                _errorMessage.value = "⚠️ חייב להגדיר תבנית לפני יצירת סידור אוטומטי!"
                return@launch
            }
            
            val (generatedSchedule, impossibleShifts) = GenericScheduleGenerator.generateSchedule(
                employees = currentEmployees,
                blocks = allBlocks,
                canOnlyBlocks = currentCanOnly,
                templateData = templateData
            )
            
            _currentSchedule.value = generatedSchedule
            _errorMessage.value = GenericScheduleGenerator.generateErrorMessage(impossibleShifts)
            
            // Use unified duplicate checking system (same as manual)
            if (!isScheduleEmpty()) {
                val weekStart = getScheduleWeekStart()
                val duplicates = checkDuplicateScheduleName(weekStart)
                
                if (duplicates.isNotEmpty()) {
                    // Show unified duplicate dialog
                    _duplicateScheduleDialog.value = DuplicateDialogState(
                        originalName = weekStart,
                        existingCount = duplicates.size,
                        pendingScheduleData = _currentSchedule.value,
                        isFromManualCreation = false
                    )
                } else {
                    // No duplicates - save directly
                    saveSchedule(weekStart)
                    finishScheduleCreation()
                }
            }
            updateTempDraftStatus() // Update after generation
            
            // Signal that generation is complete and ready to navigate
            _autoGenerationComplete.value = true
        }
    }
    
    fun resetAutoGenerationFlag() {
        _autoGenerationComplete.value = false
    }
    
    fun updateScheduleCell(cellKey: String, value: String) {
        val newSchedule = _currentSchedule.value.toMutableMap()
        newSchedule[cellKey] = if (value.isEmpty()) emptyList() else value.split(", ").map { it.trim() }
        _currentSchedule.value = newSchedule
        
        // Smart save: if editing existing schedule, auto-save changes
        if (_isEditingExistingSchedule.value && _currentScheduleId.value != null) {
            saveScheduleChanges() // Update existing record in database
        } else {
            updateTempDraftStatus() // Mark as temp draft for new schedules
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
    
    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
    
    fun saveSchedule(weekStart: String = getScheduleWeekStart()) {
        viewModelScope.launch {
            // Convert current state to JSON strings for database storage
            val gson = com.google.gson.Gson()
            
            val schedule = Schedule(
                weekStart = weekStart,
                scheduleData = gson.toJson(_currentSchedule.value),
                blocksData = gson.toJson(_blocks.value),
                canOnlyData = gson.toJson(_canOnlyBlocks.value),
                savingModeData = gson.toJson(_savingMode.value),
                createdDate = System.currentTimeMillis()
            )
            
            scheduleDao.insertSchedule(schedule)
        }
    }
    
    private fun getCurrentWeekStart(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }
    
    private fun getScheduleWeekStart(): String {
        // Use the actual week start date from the schedule for better naming
        val startDate = _weekStartDate.value
        val endDate = startDate.plusDays(6) // Saturday
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
        return "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }
    
    fun loadSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<Map<String, List<String>>>() {}.type
                val blocksType = object : com.google.gson.reflect.TypeToken<Map<String, Boolean>>() {}.type
                
                _currentSchedule.value = gson.fromJson(schedule.scheduleData, type) ?: emptyMap()
                _blocks.value = gson.fromJson(schedule.blocksData, blocksType) ?: emptyMap()
                _canOnlyBlocks.value = gson.fromJson(schedule.canOnlyData, blocksType) ?: emptyMap()
                _savingMode.value = gson.fromJson(schedule.savingModeData, blocksType) ?: emptyMap()
                _errorMessage.value = ""
                
                // Mark as editing existing schedule for smart save system
                _currentScheduleId.value = schedule.id
                _isEditingExistingSchedule.value = true
                _isEditingScheduleBlocks.value = false // Not editing blocks, just viewing
                
                // Parse and restore the week start date from the schedule
                parseAndRestoreWeekStartDate(schedule.weekStart)
                
                // IMPORTANT: Do NOT clear draft when viewing from history!
                // The user may have a work-in-progress draft that they want to return to.
                // Just mark that we're viewing an existing schedule, not creating a new draft.
                // The draft system is separate from viewing saved schedules.
                _hasTempDraft.value = false // We're not in draft mode, we're viewing saved schedule
            } catch (e: Exception) {
                // Handle JSON parsing error
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Parse the weekStart string from a saved schedule and restore _weekStartDate
     * Format expected: "dd/MM - dd/MM" (e.g., "15/12 - 21/12")
     * Falls back to next Sunday if parsing fails
     */
    private fun parseAndRestoreWeekStartDate(weekStart: String) {
        try {
            // Try to parse format "dd/MM - dd/MM" (e.g., "15/12 - 21/12")
            val parts = weekStart.split(" - ")
            if (parts.size == 2) {
                val startDatePart = parts[0].trim() // "15/12"
                val dateParts = startDatePart.split("/")
                if (dateParts.size == 2) {
                    val day = dateParts[0].toIntOrNull() ?: return
                    val month = dateParts[1].toIntOrNull() ?: return
                    
                    // Determine the year - use current year, or next year if the month is before current month
                    val today = java.time.LocalDate.now()
                    var year = today.year
                    
                    // If the month is significantly before current month, it might be next year
                    // Or if it's after, it might be from past schedules
                    val targetDate = try {
                        java.time.LocalDate.of(year, month, day)
                    } catch (e: Exception) {
                        // Invalid date, try to recover
                        return
                    }
                    
                    // If the date is more than 6 months in the future, it's probably from last year
                    if (targetDate.isAfter(today.plusMonths(6))) {
                        year -= 1
                    }
                    // If the date is more than 6 months in the past, it's probably from next year
                    else if (targetDate.isBefore(today.minusMonths(6))) {
                        year += 1
                    }
                    
                    val restoredDate = java.time.LocalDate.of(year, month, day)
                    _weekStartDate.value = restoredDate
                }
            }
        } catch (e: Exception) {
            // If parsing fails, keep the current weekStartDate (next Sunday)
            e.printStackTrace()
        }
    }
    
    // Load schedule for editing blocks (from history "back to blocks" button)
    fun loadScheduleForBlocksEditing(schedule: Schedule) {
        viewModelScope.launch {
            try {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<Map<String, List<String>>>() {}.type
                val blocksType = object : com.google.gson.reflect.TypeToken<Map<String, Boolean>>() {}.type
                
                // Load the schedule data (keep in background)
                _currentSchedule.value = gson.fromJson(schedule.scheduleData, type) ?: emptyMap()
                _blocks.value = gson.fromJson(schedule.blocksData, blocksType) ?: emptyMap()
                _canOnlyBlocks.value = gson.fromJson(schedule.canOnlyData, blocksType) ?: emptyMap()
                _savingMode.value = gson.fromJson(schedule.savingModeData, blocksType) ?: emptyMap()
                _errorMessage.value = ""
                
                // Mark as editing blocks of existing schedule
                _currentScheduleId.value = schedule.id
                _isEditingExistingSchedule.value = true
                _isEditingScheduleBlocks.value = true // Special mode: editing blocks from history
                _editedScheduleName.value = schedule.weekStart // Store schedule name/date for display
                
                // Parse and restore the week start date from the schedule
                parseAndRestoreWeekStartDate(schedule.weekStart)
                
                // IMPORTANT: Do NOT clear draft when editing from history!
                // The user may have a work-in-progress draft that they want to return to.
                _hasTempDraft.value = false // We're editing existing schedule, not a new draft
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Create a copy of the current schedule as a new schedule
    fun createScheduleCopy() {
        viewModelScope.launch {
            try {
                // Clear the current schedule ID - this will create a NEW schedule
                _currentScheduleId.value = null
                _isEditingExistingSchedule.value = false
                _isEditingScheduleBlocks.value = false
                _editedScheduleName.value = null
                
                // Keep blocks and schedule data as-is, just mark as new
                // User can now modify and save as a new schedule
                _snackbarMessage.value = "נוצר עותק - ניתן לערוך ולשמור כסידור חדש"
            } catch (e: Exception) {
                _errorMessage.value = "שגיאה ביצירת עותק: ${e.message}"
            }
        }
    }
    
    // Return to saved schedule with updated blocks (from blocking screen when editing history schedule)
    fun returnToSavedScheduleWithUpdatedBlocks() {
        viewModelScope.launch {
            try {
                val scheduleId = _currentScheduleId.value
                if (scheduleId != null) {
                    // Update the schedule in database with new blocks data
                    val schedule = scheduleDao.getScheduleById(scheduleId)
                    if (schedule != null) {
                        val gson = com.google.gson.Gson()
                        val updatedSchedule = schedule.copy(
                            blocksData = gson.toJson(_blocks.value),
                            canOnlyData = gson.toJson(_canOnlyBlocks.value),
                            savingModeData = gson.toJson(_savingMode.value)
                        )
                        scheduleDao.updateSchedule(updatedSchedule)
                    }
                    
                    // Exit blocks editing mode
                    _isEditingScheduleBlocks.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Override and create new manual schedule (discard existing schedule, create new)
    fun overrideAndCreateNewManualSchedule() {
        // Clear the editing state and create as new schedule
        _currentScheduleId.value = null
        _isEditingExistingSchedule.value = false
        _isEditingScheduleBlocks.value = false
        
        // Clear current schedule but keep blocks
        _currentSchedule.value = emptyMap()
        
        // Proceed to manual creation
        prepareForManualCreation()
    }
    
    // Navigate to blocks editing from preview (set editing mode if this is existing schedule)
    fun navigateToBlocksEditingFromPreview() {
        if (_isEditingExistingSchedule.value && _currentScheduleId.value != null) {
            // This is an existing schedule - enable blocks editing mode
            _isEditingScheduleBlocks.value = true
        } else {
            // This is a new schedule - just cancel and return
            cancelAutomaticSchedule()
        }
    }
    
    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleDao.deleteSchedule(schedule)
        }
    }
    
    fun updateScheduleName(schedule: Schedule, newName: String) {
        viewModelScope.launch {
            // Create a new schedule with updated name
            val updatedSchedule = schedule.copy(weekStart = newName)
            // Update in database
            scheduleDao.deleteSchedule(schedule) // Remove old
            scheduleDao.insertSchedule(updatedSchedule) // Insert updated
        }
    }
    
    // Smart save system functions
    
    fun isScheduleEmpty(): Boolean {
        return _currentSchedule.value.isEmpty() || 
               _currentSchedule.value.all { it.value.isEmpty() }
    }
    
    suspend fun checkDuplicateScheduleName(weekStart: String): List<Schedule> {
        return scheduleDao.getSchedulesByWeekStart(weekStart)
    }
    
    // Smart save - updates existing schedule instead of creating new one
    fun saveScheduleChanges() {
        viewModelScope.launch {
            val currentId = _currentScheduleId.value
            if (currentId != null && _isEditingExistingSchedule.value) {
                // Update existing schedule
                val gson = com.google.gson.Gson()
                val existingSchedule = scheduleDao.getScheduleById(currentId)
                
                if (existingSchedule != null) {
                    val updatedSchedule = existingSchedule.copy(
                        scheduleData = gson.toJson(_currentSchedule.value),
                        blocksData = gson.toJson(_blocks.value),
                        canOnlyData = gson.toJson(_canOnlyBlocks.value),
                        savingModeData = gson.toJson(_savingMode.value)
                    )
                    scheduleDao.updateSchedule(updatedSchedule)
                }
            } else {
                // Create new schedule (fallback to regular save)
                saveSchedule()
            }
        }
    }
    
    // Create new schedule with duplicate handling
    suspend fun saveNewScheduleWithDuplicateCheck(weekStart: String): SaveResult {
        // Check if schedule is empty
        if (isScheduleEmpty()) {
            return SaveResult.Empty
        }
        
        // Check for duplicates
        val duplicates = checkDuplicateScheduleName(weekStart)
        if (duplicates.isNotEmpty()) {
            return SaveResult.Duplicate(duplicates.size, weekStart)
        }
        
        // Save new schedule
        saveSchedule(weekStart)
        return SaveResult.Success
    }
    
    // Force save with version number (for duplicate resolution)
    fun saveScheduleWithVersion(baseWeekStart: String, version: Int) {
        val versionedName = if (version > 0) "$baseWeekStart ($version)" else baseWeekStart
        saveSchedule(versionedName)
    }
    
    // Helper function to finish schedule creation process
    private fun finishScheduleCreation() {
        // DON'T clear blocks - they need to stay for preview and history!
        // User can manually reset if they want to start fresh
        
        // Clear draft - schedule is now saved in history!
        clearDraft()
        
        // Keep currentScheduleId and isEditingExistingSchedule - they're needed for history
    }
    
    // Convert "can-only" blocks to "cannot" blocks for all other positions
    private fun convertCanOnlyToCannotBlocks(): Map<String, Boolean> {
        val currentBlocks = _blocks.value.toMutableMap()
        val currentCanOnly = _canOnlyBlocks.value
        val currentEmployees = employees.value
        
        // FIXED: Group all "can-only" cells by employee first
        val canOnlyByEmployee = mutableMapOf<String, MutableList<Pair<String, String>>>()
        
        currentCanOnly.forEach { (canOnlyKey, isCanOnly) ->
            if (isCanOnly) {
                // Parse the key: "employeeName-day-shift"
                val parts = canOnlyKey.split("-")
                if (parts.size >= 3) {
                    val employeeName = parts[0]
                    val day = parts[1]
                    val shift = parts[2]
                    
                    // Add to employee's list
                    if (!canOnlyByEmployee.containsKey(employeeName)) {
                        canOnlyByEmployee[employeeName] = mutableListOf()
                    }
                    canOnlyByEmployee[employeeName]!!.add(Pair(day, shift))
                }
            }
        }
        
        // Now, for each employee with "can-only" restrictions, block all OTHER positions
        canOnlyByEmployee.forEach { (employeeName, allowedPositions) ->
            val employee = currentEmployees.find { it.name == employeeName }
            if (employee != null) {
                // Block this employee from ALL day-shift combinations EXCEPT the allowed ones
                ShiftDefinitions.daysOfWeek.forEach { day ->
                    // Get available shifts for this day
                    val availableShifts = getShiftsForDay(day, _savingMode.value[day] == true)
                    
                    availableShifts.forEach { shift ->
                        val blockKey = "$employeeName-$day-$shift"
                        
                        // Check if this position is in the allowed list
                        val isAllowedPosition = allowedPositions.any { it.first == day && it.second == shift }
                        
                        // If this is NOT an allowed position, block it
                        if (!isAllowedPosition) {
                            // Don't override existing manual blocks or automatic Shabbat blocks
                            if (!currentBlocks.containsKey(blockKey)) {
                                val isAutomaticShabbatBlock = employee.shabbatObserver &&
                                        ShiftDefinitions.shabbatBlockedShifts.contains("$day-$shift")
                                
                                if (!isAutomaticShabbatBlock) {
                                    currentBlocks[blockKey] = true
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return currentBlocks
    }
    
    // Get available shifts for a specific day
    private fun getShiftsForDay(day: String, isSavingMode: Boolean): List<String> {
        return when (day) {
            "שישי" -> if (isSavingMode) {
                listOf("בוקר", "בוקר-קצר", "לילה-ארוך") // שישי מצב חסכון - גם בוקר רגיל וגם קצר
            } else {
                listOf("בוקר", "בוקר-קצר", "צהריים", "לילה") // שישי רגיל - גם בוקר רגיל וגם קצר
            }
            "שבת" -> listOf("בוקר", "צהריים", "לילה") // No saving mode for Saturday
            else -> if (isSavingMode) {
                listOf("בוקר-ארוך", "לילה-ארוך")
            } else {
                listOf("בוקר", "בוקר-ארוך", "צהריים", "לילה")
            }
        }
    }
    
    // Dialog actions for duplicate resolution
    fun onDuplicateDialogOverwrite() {
        val dialogState = _duplicateScheduleDialog.value ?: return
        viewModelScope.launch {
            // Delete existing schedules with same name
            val existingSchedules = checkDuplicateScheduleName(dialogState.originalName)
            existingSchedules.forEach { scheduleDao.deleteSchedule(it) }
            
            // Save new schedule with original name
            saveSchedule(dialogState.originalName)
            finishScheduleCreation()
            
            // Close dialog
            _duplicateScheduleDialog.value = null
        }
    }
    
    fun onDuplicateDialogCreateNew() {
        val dialogState = _duplicateScheduleDialog.value ?: return
        viewModelScope.launch {
            // Save with version number
            val versionedName = "${dialogState.originalName} (${dialogState.existingCount})"
            saveSchedule(versionedName)
            finishScheduleCreation()
            
            // Close dialog
            _duplicateScheduleDialog.value = null
        }
    }
    
    fun onDuplicateDialogDismiss() {
        _duplicateScheduleDialog.value = null
    }
    
    // Prepare data for manual creation screen - keep can-only as is (no conversion!)
    fun prepareForManualCreation() {
        // NEW: Don't convert! Keep both blocks and canOnlyBlocks for manual screen
        // This way user sees:
        // - Red cells for "לא יכול" (blocks)
        // - Blue cells for "יכול" (canOnlyBlocks)
        updateTempDraftStatus()
    }
    
    // Result types for save operations
    sealed class SaveResult {
        object Success : SaveResult()
        object Empty : SaveResult()
        data class Duplicate(val count: Int, val originalName: String) : SaveResult()
    }
    
    enum class BlockingMode {
        CANNOT, CAN_ONLY
    }
    
    // Temp Draft Management
    private fun updateTempDraftStatus() {
        // IMPORTANT: If viewing or editing an existing schedule from history,
        // do NOT update temp draft status - we're not in draft mode!
        if (_isEditingExistingSchedule.value && _currentScheduleId.value != null) {
            // We're viewing/editing an existing saved schedule from history
            // The data in state is the history schedule, NOT a new draft
            _hasTempDraft.value = false
            
            // If specifically editing blocks, save changes to the schedule
            if (_isEditingScheduleBlocks.value) {
                saveScheduleChanges()
            }
            return
        }
        
        // Check if there's any temp work (manual blocks, schedules, etc.)
        // Ignore automatic Shabbat blocks - they don't count as temp draft
        val currentEmployees = employees.value
        
        // Count only manual blocks (not automatic Shabbat blocks)
        val manualBlocks = _blocks.value.filter { (key, _) ->
            val parts = key.split("-")
            if (parts.size >= 3) {
                val employeeName = parts[0]
                val day = parts[1]
                val shift = parts[2]
                val shiftKey = "$day-$shift"
                
                // Find employee
                val employee = currentEmployees.find { it.name == employeeName }
                val isAutomaticShabbatBlock = employee?.shabbatObserver == true &&
                        ShiftDefinitions.shabbatBlockedShifts.contains(shiftKey)
                
                // Only count if it's NOT an automatic Shabbat block
                !isAutomaticShabbatBlock
            } else {
                true // Count if we can't parse (safety)
            }
        }
        
        val hasManualBlocks = manualBlocks.isNotEmpty()
        val hasCanOnly = _canOnlyBlocks.value.isNotEmpty() 
        val hasSchedule = _currentSchedule.value.isNotEmpty()
        
        val hasDraft = hasManualBlocks || hasCanOnly || hasSchedule
        _hasTempDraft.value = hasDraft
        
        // Track if draft has manual assignments (schedule with employee assignments)
        _draftHasManualAssignments.value = hasSchedule
        
        // If no draft data, delete any saved draft from DB
        if (!hasDraft) {
            deleteDraft()
        }
    }
    
    fun checkTempDraftOnStart() {
        updateTempDraftStatus()
    }
    
    fun continueTempDraft() {
        // User wants to continue existing draft - no action needed, data is already loaded
        updateTempDraftStatus()
    }
    
    fun startNewSchedule() {
        // User wants to start completely fresh - clear everything and reset smart save system
        clearAllBlocks()
        clearManualSchedule()
        clearDraft() // Delete draft from DB
        // Reset smart save system for new schedule
        _currentScheduleId.value = null
        _isEditingExistingSchedule.value = false
    }
    
    // Save draft when app closes - persist temp work to database
    fun saveDraftOnAppClose() {
        viewModelScope.launch {
            try {
                // IMPORTANT: Don't save draft if we're viewing/editing a history schedule!
                // This would overwrite the user's actual draft with the history schedule data
                if (_isEditingExistingSchedule.value && _currentScheduleId.value != null) {
                    // We're viewing a history schedule - don't touch the draft
                    return@launch
                }
                
                // Only save if there's actual temp draft data
                if (_hasTempDraft.value) {
                    val gson = com.google.gson.Gson()
                    
                    // Create a special draft schedule with marker name
                    val draftSchedule = Schedule(
                        id = 0, // Will be auto-generated or replaced
                        weekStart = "__TEMP_DRAFT__", // Special marker for draft
                        scheduleData = gson.toJson(_currentSchedule.value),
                        blocksData = gson.toJson(_blocks.value),
                        canOnlyData = gson.toJson(_canOnlyBlocks.value),
                        savingModeData = gson.toJson(_savingMode.value),
                        createdDate = System.currentTimeMillis()
                    )
                    
                    // Delete any existing draft first
                    val existingDrafts = scheduleDao.getSchedulesByWeekStart("__TEMP_DRAFT__")
                    existingDrafts.forEach { scheduleDao.deleteSchedule(it) }
                    
                    // Save new draft
                    scheduleDao.insertSchedule(draftSchedule)
                }
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
    
    // Load draft when app starts
    fun loadDraftOnAppStart() {
        viewModelScope.launch {
            try {
                val drafts = scheduleDao.getSchedulesByWeekStart("__TEMP_DRAFT__")
                if (drafts.isNotEmpty()) {
                    val draft = drafts.first()
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<Map<String, List<String>>>() {}.type
                    val blocksType = object : com.google.gson.reflect.TypeToken<Map<String, Boolean>>() {}.type
                    
                    _currentSchedule.value = gson.fromJson(draft.scheduleData, type) ?: emptyMap()
                    _blocks.value = gson.fromJson(draft.blocksData, blocksType) ?: emptyMap()
                    _canOnlyBlocks.value = gson.fromJson(draft.canOnlyData, blocksType) ?: emptyMap()
                    _savingMode.value = gson.fromJson(draft.savingModeData, blocksType) ?: emptyMap()
                    
                    updateTempDraftStatus()
                }
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
    
    // Delete draft from database
    private fun deleteDraft() {
        viewModelScope.launch {
            try {
                val drafts = scheduleDao.getSchedulesByWeekStart("__TEMP_DRAFT__")
                drafts.forEach { scheduleDao.deleteSchedule(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Delete draft and clear temp status (called when schedule is saved)
    fun clearDraft() {
        deleteDraft()
        _hasTempDraft.value = false
    }
    
    // ============ Shift Template Management Functions ============
    
    init {
        // Check if we have an active template - no automatic creation!
        // User must create their own template before creating schedules
        viewModelScope.launch {
            val template = dynamicShiftManager.getActiveTemplateDataSync()
            _hasActiveTemplate.value = template != null
        }
    }
    
    fun loadTemplateForEditing() {
        viewModelScope.launch {
            val template = shiftTemplateDao.getActiveTemplateSync()
            if (template != null) {
                // Load existing template
                _editingShiftRows.value = shiftTemplateDao.getShiftRowsSync(template.id)
                
                // Load ALL day columns (not just enabled) - FIXED: show all 7 days
                val existingColumns = shiftTemplateDao.getAllDayColumnsSync(template.id)
                
                // Ensure all 7 days exist (for templates created before this fix)
                val allDayNames = listOf(
                    "ראשון" to "Sunday",
                    "שני" to "Monday",
                    "שלישי" to "Tuesday",
                    "רביעי" to "Wednesday",
                    "חמישי" to "Thursday",
                    "שישי" to "Friday",
                    "שבת" to "Saturday"
                )
                
                // Create a map of existing columns by day index
                val existingMap = existingColumns.associateBy { it.dayIndex }
                
                // Build list with all 7 days, using existing data or creating new entries
                _editingDayColumns.value = allDayNames.mapIndexed { index, (hebrew, english) ->
                    existingMap[index] ?: DayColumn(
                        templateId = template.id,
                        dayIndex = index,
                        dayNameHebrew = hebrew,
                        dayNameEnglish = english,
                        isEnabled = true
                    )
                }
            } else {
                // No template - initialize with default shifts (user can edit)
                _editingShiftRows.value = listOf(
                    ShiftRow(
                        templateId = 0,
                        orderIndex = 0,
                        shiftName = "בוקר",
                        shiftHours = "07:00-15:00",
                        displayName = "בוקר (07:00-15:00)"
                    ),
                    ShiftRow(
                        templateId = 0,
                        orderIndex = 1,
                        shiftName = "צהריים",
                        shiftHours = "15:00-23:00",
                        displayName = "צהריים (15:00-23:00)"
                    ),
                    ShiftRow(
                        templateId = 0,
                        orderIndex = 2,
                        shiftName = "לילה",
                        shiftHours = "23:00-07:00",
                        displayName = "לילה (23:00-07:00)"
                    )
                )
                // Initialize with all 7 days enabled
                val dayNames = listOf(
                    "ראשון" to "Sunday",
                    "שני" to "Monday",
                    "שלישי" to "Tuesday",
                    "רביעי" to "Wednesday",
                    "חמישי" to "Thursday",
                    "שישי" to "Friday",
                    "שבת" to "Saturday"
                )
                _editingDayColumns.value = dayNames.mapIndexed { index, (hebrew, english) ->
                    DayColumn(
                        templateId = 0,
                        dayIndex = index,
                        dayNameHebrew = hebrew,
                        dayNameEnglish = english,
                        isEnabled = true
                    )
                }
            }
        }
    }
    
    fun addShiftRow(shiftName: String, shiftHours: String) {
        val currentRows = _editingShiftRows.value
        if (currentRows.size < 8 && shiftName.isNotBlank() && shiftHours.isNotBlank()) {
            val newRow = com.hananel.workschedule.data.ShiftRow(
                templateId = 0, // Will be set when saving
                orderIndex = currentRows.size,
                shiftName = shiftName,
                shiftHours = shiftHours,
                displayName = "$shiftName ($shiftHours)"
            )
            _editingShiftRows.value = currentRows + newRow
        }
    }
    
    fun updateShiftRow(index: Int, shiftName: String, shiftHours: String) {
        val currentRows = _editingShiftRows.value.toMutableList()
        if (index in currentRows.indices) {
            val row = currentRows[index]
            currentRows[index] = row.copy(
                shiftName = shiftName,
                shiftHours = shiftHours,
                displayName = "$shiftName ($shiftHours)"
            )
            _editingShiftRows.value = currentRows
        }
    }
    
    fun editShiftRow(index: Int, shiftName: String, shiftHours: String) {
        val currentRows = _editingShiftRows.value.toMutableList()
        if (index in currentRows.indices) {
            currentRows[index] = currentRows[index].copy(
                shiftName = shiftName,
                shiftHours = shiftHours,
                displayName = "$shiftName ($shiftHours)"
            )
            _editingShiftRows.value = currentRows
        }
    }
    
    fun deleteShiftRow(index: Int) {
        val currentRows = _editingShiftRows.value.toMutableList()
        if (index in currentRows.indices && currentRows.size > 2) {
            currentRows.removeAt(index)
            // Update order indices
            currentRows.forEachIndexed { idx, row ->
                currentRows[idx] = row.copy(orderIndex = idx)
            }
            _editingShiftRows.value = currentRows
        }
    }
    
    fun moveShiftRow(fromIndex: Int, toIndex: Int) {
        val currentRows = _editingShiftRows.value.toMutableList()
        if (fromIndex in currentRows.indices && toIndex in currentRows.indices) {
            val movedRow = currentRows.removeAt(fromIndex)
            currentRows.add(toIndex, movedRow)
            // Update order indices
            currentRows.forEachIndexed { idx, row ->
                currentRows[idx] = row.copy(orderIndex = idx)
            }
            _editingShiftRows.value = currentRows
        }
    }
    
    fun toggleDayColumn(index: Int) {
        val currentColumns = _editingDayColumns.value.toMutableList()
        if (index in currentColumns.indices) {
            val column = currentColumns[index]
            val enabledCount = currentColumns.count { it.isEnabled }
            
            // Don't allow disabling if it would go below 4 enabled days
            if (column.isEnabled && enabledCount <= 4) {
                return
            }
            
            currentColumns[index] = column.copy(isEnabled = !column.isEnabled)
            _editingDayColumns.value = currentColumns
        }
    }
    
    fun saveTemplate() {
        viewModelScope.launch {
            try {
                val allRows = _editingShiftRows.value
                val allColumns = _editingDayColumns.value // Save ALL columns (enabled and disabled)
                val enabledColumns = allColumns.filter { it.isEnabled }
                
                // Filter out empty rows (no name or no hours)
                val validRows = allRows.filter { row ->
                    row.shiftName.isNotBlank() && row.shiftHours.isNotBlank()
                }
                
                // Validate - at least 2 valid shifts and 4 enabled days
                if (validRows.size < 2 || enabledColumns.size < 4) {
                    return@launch
                }
                
                // Deactivate all existing templates
                shiftTemplateDao.deactivateAllTemplates()
                
                // Create new template with valid rows only
                val template = com.hananel.workschedule.data.ShiftTemplate(
                    name = "תבנית מותאמת",
                    rowCount = validRows.size,
                    columnCount = enabledColumns.size, // Count only enabled columns
                    isActive = true
                )
                
                // Save only valid rows (no empty ones) and ALL columns (for editing later)
                shiftTemplateDao.createCompleteTemplate(template, validRows, allColumns)
                _hasActiveTemplate.value = true
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun createDefaultTemplate() {
        viewModelScope.launch {
            dynamicShiftManager.createDefaultTemplate()
            _hasActiveTemplate.value = true
        }
    }
}

class ScheduleViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
