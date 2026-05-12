package com.shishusneh

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.shishusneh.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val repository: ShishuRepository,
    private val workManager: WorkManager
) : ViewModel() {

    // --- Profile & Navigation State ---
    val hasProfile: StateFlow<Boolean> = repository.hasProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val babyProfile: StateFlow<BabyProfile?> = repository.getBabyProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Health & Growth Data Streams ---
    val growthRecords: StateFlow<List<GrowthRecord>> = repository.growthRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaccines: StateFlow<List<VaccinationRecord>> = repository.vaccines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<Milestone>> = repository.milestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Milestone Filter Settings ---
    private val _showAllMilestones = MutableStateFlow(false)
    val showAllMilestones: StateFlow<Boolean> = _showAllMilestones.asStateFlow()

    fun toggleShowAllMilestones() {
        _showAllMilestones.value = !_showAllMilestones.value
    }

    // --- Filtered Milestones (Weekly Perspective) ---
    val filteredMilestones: StateFlow<List<Milestone>> = combine(
        babyProfile, 
        milestones, 
        _showAllMilestones
    ) { profile: BabyProfile?, allMilestones: List<Milestone>, showAll: Boolean ->
        if (profile == null) return@combine emptyList<Milestone>()
        val babyAgeWeeks = getBabyAgeInWeeks(profile.dateOfBirth)
        
        allMilestones.filter { milestone ->
            if (showAll) return@filter true
            
            val milestoneWeek = parseToWeeks(milestone.age)
            
            // Show milestones that are:
            // 1. For the current week or any past week (Overdue/Completed)
            // 2. Upcoming milestones within a generous 12-week window (3 months)
            // 3. Any completed milestone (to show photos in the "baby book")
            val isCurrentOrPast = milestoneWeek <= babyAgeWeeks
            val isUpcoming = milestoneWeek > babyAgeWeeks && milestoneWeek <= babyAgeWeeks + 12
            
            isCurrentOrPast || isUpcoming || milestone.isCompleted
        }.sortedBy { parseToWeeks(it.age) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun parseToWeeks(ageStr: String): Int {
        val cleaned = ageStr.trim().lowercase()
        // Extract the numeric value correctly even for decimals like "1.5 Years"
        val value = cleaned.split(Regex("\\s+")).firstOrNull()?.toDoubleOrNull() ?: 0.0
        return when {
            cleaned.contains("week") -> value.toInt()
            cleaned.contains("month") -> (value * 4.34).toInt() // 30.4 days avg
            cleaned.contains("year") -> (value * 52.17).toInt() // 365.25 days avg
            else -> value.toInt()
        }
    }

    // --- Growth Restriction Logic ---
    val daysUntilNextGrowth: StateFlow<Long> = growthRecords.map { records ->
        if (records.isEmpty()) 0L
        else {
            val lastDate = records.lastOrNull()?.date ?: 0L
            val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000L
            val nextDate = lastDate + thirtyDaysMillis
            val remaining = nextDate - System.currentTimeMillis()
            if (remaining > 0) (remaining / (1000 * 60 * 60 * 24)) + 1 else 0L
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // --- Localization State ---
    var isHindi by mutableStateOf(false)
        private set

    fun toggleLanguage() {
        isHindi = !isHindi
    }

    init {
        viewModelScope.launch {
            repository.seedData()
            
            // Sync vaccines only when necessary to avoid infinite loops
            var lastSyncedDob = -1L
            babyProfile.collect { profile ->
                if (profile != null && profile.dateOfBirth != lastSyncedDob) {
                    syncVaccineDates(profile.dateOfBirth)
                    lastSyncedDob = profile.dateOfBirth
                }
            }
        }
        setupPeriodicReminders()
    }

    private suspend fun syncVaccineDates(dobMillis: Long) {
        val currentVaccines = vaccines.value
        currentVaccines.forEach { vaccine ->
            val expectedDate = calculateDueDate(dobMillis, vaccine.scheduledTimeframe)
            if (vaccine.dueDateMillis != expectedDate && !vaccine.isCompleted) {
                repository.saveVaccine(vaccine.copy(dueDateMillis = expectedDate))
            }
        }
        scheduleVaccineReminders(currentVaccines.filter { !it.isCompleted })
    }

    private fun scheduleVaccineReminders(vaccineList: List<VaccinationRecord>) {
        vaccineList.forEach { vaccine ->
            val now = System.currentTimeMillis()
            val dueDate = vaccine.dueDateMillis
            
            if (dueDate > 0 && !vaccine.isCompleted) {
                val oneDayMillis = 24 * 60 * 60 * 1000L
                val reminderTime = dueDate - oneDayMillis
                val delay = if (reminderTime <= now) 0L else reminderTime - now
                
                if (dueDate > now - oneDayMillis) {
                    val data = workDataOf(
                        "vaccine_name" to vaccine.diseaseName,
                        "message" to "Upcoming: ${vaccine.diseaseName} is due soon!"
                    )
                    
                    val request = OneTimeWorkRequestBuilder<VaccineReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .build()

                    workManager.enqueueUniqueWork(
                        "reminder_${vaccine.id}",
                        ExistingWorkPolicy.REPLACE,
                        request
                    )
                }
            }
        }
    }

    private fun setupPeriodicReminders() {
        val request = PeriodicWorkRequestBuilder<VaccineReminderWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()

        workManager.enqueueUniquePeriodicWork(
            "vaccine_periodic_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // --- Age Calculation Helpers ---
    fun getBabyAgeInMonths(dobMillis: Long): Int {
        if (dobMillis <= 0L) return 0
        val diff = System.currentTimeMillis() - dobMillis
        val days = diff / (1000 * 60 * 60 * 24)
        return (days / 30).toInt()
    }

    fun getBabyAgeInWeeks(dobMillis: Long): Int {
        if (dobMillis <= 0L) return 0
        val diff = System.currentTimeMillis() - dobMillis
        val days = diff / (1000 * 60 * 60 * 24)
        return (days / 7).toInt()
    }

    // --- Registration & Profile Management ---
    fun registerBaby(name: String, dobString: String, gender: String) {
        viewModelScope.launch {
            val dobMillis = parseDob(dobString)

            val newProfile = BabyProfile(
                name = name,
                dateOfBirth = dobMillis,
                gender = gender
            )
            repository.saveBabyProfile(newProfile)

            val autoVaccines = listOf(
                VaccinationRecord(diseaseName = "BCG", description = "Tuberculosis Prevention", scheduledTimeframe = "At Birth", dueDateMillis = calculateDueDate(dobMillis, "At Birth")),
                VaccinationRecord(diseaseName = "OPV-0 & Hep-B", description = "Polio & Hepatitis B", scheduledTimeframe = "At Birth", dueDateMillis = calculateDueDate(dobMillis, "At Birth")),
                VaccinationRecord(diseaseName = "OPV-1 & Penta-1", description = "Polio, DPT, HepB, HiB", scheduledTimeframe = "6 Weeks", dueDateMillis = calculateDueDate(dobMillis, "6 Weeks")),
                VaccinationRecord(diseaseName = "OPV-2 & Penta-2", description = "Polio, DPT, HepB, HiB", scheduledTimeframe = "10 Weeks", dueDateMillis = calculateDueDate(dobMillis, "10 Weeks")),
                VaccinationRecord(diseaseName = "OPV-3 & Penta-3", description = "Polio, DPT, HepB, HiB", scheduledTimeframe = "14 Weeks", dueDateMillis = calculateDueDate(dobMillis, "14 Weeks")),
                VaccinationRecord(diseaseName = "Measles/MR 1", description = "Measles & Rubella Prevention", scheduledTimeframe = "9 Months", dueDateMillis = calculateDueDate(dobMillis, "9 Months"))
            )

            autoVaccines.forEach { vaccine ->
                repository.saveVaccine(vaccine)
            }
        }
    }

    fun updateBabyProfile(name: String, dobString: String, gender: String) {
        viewModelScope.launch {
            val currentProfile = babyProfile.value ?: return@launch
            val newDob = parseDob(dobString)
            
            val updatedProfile = currentProfile.copy(
                name = name,
                dateOfBirth = newDob,
                gender = gender
            )
            repository.saveBabyProfile(updatedProfile)
        }
    }

    fun calculateDueDate(dobMillis: Long, timeframe: String): Long {
        val weekInMillis = 7L * 24 * 60 * 60 * 1000
        val monthInMillis = 30L * 24 * 60 * 60 * 1000
        return when (timeframe) {
            "At Birth" -> dobMillis
            "6 Weeks" -> dobMillis + (6 * weekInMillis)
            "10 Weeks" -> dobMillis + (10 * weekInMillis)
            "14 Weeks" -> dobMillis + (14 * weekInMillis)
            "9 Months" -> dobMillis + (9 * monthInMillis)
            else -> dobMillis
        }
    }

    private fun parseDob(dobString: String): Long {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dobString)?.time
                ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun updateBabyImage(uri: String) {
        viewModelScope.launch {
            repository.updateBabyProfileImage(uri)
        }
    }

    fun addGrowth(weight: String, height: String) {
        if (daysUntilNextGrowth.value > 0) return
        val w = weight.toFloatOrNull() ?: return
        val h = height.toFloatOrNull() ?: return
        viewModelScope.launch {
            repository.addGrowth(w, h)
        }
    }

    fun toggleVaccine(vaccine: VaccinationRecord) {
        viewModelScope.launch {
            val updated = vaccine.copy(isCompleted = !vaccine.isCompleted)
            repository.saveVaccine(updated)
        }
    }

    fun toggleMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.saveMilestone(milestone.copy(isCompleted = !milestone.isCompleted))
        }
    }

    fun updateMilestoneImage(milestone: Milestone, uri: String) {
        viewModelScope.launch {
            repository.saveMilestone(milestone.copy(imageUri = uri))
        }
    }
}

class ViewModelFactory(
    private val repository: ShishuRepository,
    private val workManager: WorkManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, workManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
