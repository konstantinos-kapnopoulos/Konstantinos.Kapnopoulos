package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CigaretteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDay(
    val dateString: String?, // "yyyy-MM-dd" or null for padding
    val dayNumber: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val count: Int
)

data class DailyStat(val dayLabel: String, val count: Int)
data class WeeklyStat(val weekLabel: String, val count: Int)
data class MonthlyStat(val monthLabel: String, val count: Int)

class CigaretteViewModel(
    private val application: Application,
    private val repository: CigaretteRepository
) : AndroidViewModel(application) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val todayString = dateFormatter.format(Date())

    private val prefs: SharedPreferences =
        application.getSharedPreferences("cigarette_prefs", Context.MODE_PRIVATE)

    // Goal and Reminder States
    private val _dailyGoal = MutableStateFlow(prefs.getInt("daily_goal", 20))
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(prefs.getBoolean("reminders_enabled", true))
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    // Currently selected date for the main counter
    private val _selectedDate = MutableStateFlow(todayString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Currently viewed month calendar
    private val _viewedCalendar = MutableStateFlow<Calendar>(Calendar.getInstance())
    val viewedCalendar: StateFlow<Calendar> = _viewedCalendar.asStateFlow()

    // Map of date -> cigarette count from Room
    val recordsMap: StateFlow<Map<String, Int>> = repository.allRecords
        .map { list -> list.associate { it.date to it.count } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Current count for the selected date
    val currentCount: StateFlow<Int> = combine(_selectedDate, recordsMap) { date, map ->
        map[date] ?: 0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Computed days for the calendar view
    val calendarDays: StateFlow<List<CalendarDay>> = combine(
        _viewedCalendar,
        _selectedDate,
        recordsMap
    ) { calendar, selected, records ->
        generateCalendarDays(calendar, selected, records)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- STATISTICS STREAM COMPUTATIONS ---

    val statsDaily: StateFlow<List<DailyStat>> = recordsMap.map { records ->
        val list = mutableListOf<DailyStat>()
        val dayFormat = SimpleDateFormat("E", Locale("el")) // Greek short day (ΔΕΥ, ΤΡΙ)
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        for (i in 6 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = keyFormat.format(tempCal.time)
            val count = records[dateStr] ?: 0
            val label = dayFormat.format(tempCal.time).uppercase()
            list.add(DailyStat(label, count))
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statsWeekly: StateFlow<List<WeeklyStat>> = recordsMap.map { records ->
        val list = mutableListOf<WeeklyStat>()
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        for (i in 3 downTo 0) {
            var weeklySum = 0
            val calStart = Calendar.getInstance()
            calStart.add(Calendar.WEEK_OF_YEAR, -i)
            calStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            
            val tempCal = calStart.clone() as Calendar
            for (day in 0..6) {
                val dateStr = keyFormat.format(tempCal.time)
                weeklySum += records[dateStr] ?: 0
                tempCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val label = if (i == 0) "ΑΥΤΗ" else "${4-i}η ΕΒΔ"
            list.add(WeeklyStat(label, weeklySum))
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statsMonthly: StateFlow<List<MonthlyStat>> = recordsMap.map { records ->
        val list = mutableListOf<MonthlyStat>()
        val monthFormat = SimpleDateFormat("MMM", Locale("el")) // Greek short month (ΙΑΝ, ΦΕΒ)
        
        for (i in 5 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.MONTH, -i)
            val year = tempCal.get(Calendar.YEAR)
            val month = tempCal.get(Calendar.MONTH)
            
            var monthlySum = 0
            val daysInMonthCal = Calendar.getInstance()
            daysInMonthCal.set(year, month, 1)
            val maxDays = daysInMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            for (day in 1..maxDays) {
                daysInMonthCal.set(Calendar.DAY_OF_MONTH, day)
                val dateStr = dateFormatter.format(daysInMonthCal.time)
                monthlySum += records[dateStr] ?: 0
            }
            
            val label = monthFormat.format(tempCal.time).uppercase().removeSuffix(".")
            list.add(MonthlyStat(label, monthlySum))
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val averageDaily: StateFlow<Double> = recordsMap.map { records ->
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var total = 0
        val daysCount = 14
        for (i in 0 until daysCount) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = keyFormat.format(tempCal.time)
            total += records[dateStr] ?: 0
        }
        total.toDouble() / daysCount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val reductionRate: StateFlow<Double?> = recordsMap.map { records ->
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var current7DaysTotal = 0
        var previous7DaysTotal = 0
        
        for (i in 0 until 7) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = keyFormat.format(tempCal.time)
            current7DaysTotal += records[dateStr] ?: 0
        }
        
        for (i in 7 until 14) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = keyFormat.format(tempCal.time)
            previous7DaysTotal += records[dateStr] ?: 0
        }
        
        if (previous7DaysTotal > 0) {
            ((previous7DaysTotal - current7DaysTotal).toDouble() / previous7DaysTotal) * 100.0
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        createNotificationChannel()
    }

    // --- GOALS AND REMINDERS API ---

    fun updateDailyGoal(goal: Int) {
        val boundedGoal = goal.coerceIn(1, 40)
        _dailyGoal.value = boundedGoal
        prefs.edit().putInt("daily_goal", boundedGoal).apply()
    }

    fun updateRemindersEnabled(enabled: Boolean) {
        _remindersEnabled.value = enabled
        prefs.edit().putBoolean("reminders_enabled", enabled).apply()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Όρια Καπνίσματος"
            val descriptionText = "Ειδοποιήσεις για το ημερήσιο όριο τσιγάρων"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CIGARETTE_GOAL_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun triggerProgressNotification(newCount: Int) {
        if (!_remindersEnabled.value) return
        val goal = _dailyGoal.value
        
        val title: String
        val message: String
        
        when {
            newCount == goal -> {
                title = "Έφτασες το όριο σου! ⚠️"
                message = "Έχεις καπνίσει $newCount τσιγάρα. Αυτό είναι το μέγιστο ημερήσιο όριο που έχεις θέσει."
            }
            newCount == (goal * 0.8).toInt() && goal >= 5 -> {
                title = "Πλησιάζεις το όριο σου! ⚠️"
                message = "Έχεις καπνίσει $newCount τσιγάρα (80% του ορίου σου των $goal τσιγάρων)."
            }
            newCount > goal -> {
                title = "Ξεπέρασες το όριο! 🚨"
                message = "Έχεις καπνίσει $newCount τσιγάρα, ξεπερνώντας το ημερήσιο όριο σου κατά ${newCount - goal}."
            }
            else -> return
        }

        val builder = NotificationCompat.Builder(application, "CIGARETTE_GOAL_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(application)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        application,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManager.notify(1001, builder.build())
                }
            } else {
                notificationManager.notify(1001, builder.build())
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    // --- RECORD CRUD OPERATIONS ---

    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
    }

    fun incrementCount() {
        val date = _selectedDate.value
        val count = currentCount.value
        val newCount = count + 1
        viewModelScope.launch {
            repository.saveRecord(date, newCount)
            if (date == todayString) {
                triggerProgressNotification(newCount)
            }
        }
    }

    fun decrementCount() {
        val date = _selectedDate.value
        val count = currentCount.value
        if (count > 0) {
            viewModelScope.launch {
                repository.saveRecord(date, count - 1)
            }
        }
    }

    fun setCustomCount(count: Int) {
        val date = _selectedDate.value
        if (count >= 0) {
            viewModelScope.launch {
                repository.saveRecord(date, count)
                if (date == todayString) {
                    triggerProgressNotification(count)
                }
            }
        }
    }

    fun selectToday() {
        _selectedDate.value = todayString
        _viewedCalendar.value = Calendar.getInstance()
    }

    fun prevMonth() {
        val cal = _viewedCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _viewedCalendar.value = cal
    }

    fun nextMonth() {
        val cal = _viewedCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _viewedCalendar.value = cal
    }

    private fun generateCalendarDays(
        calendarInstance: Calendar,
        selectedDate: String,
        records: Map<String, Int>
    ): List<CalendarDay> {
        val list = mutableListOf<CalendarDay>()

        val cal = calendarInstance.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        val emptyDaysBefore = when (firstDayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        for (i in 0 until emptyDaysBefore) {
            list.add(
                CalendarDay(
                    dateString = null,
                    dayNumber = 0,
                    isToday = false,
                    isSelected = false,
                    count = 0
                )
            )
        }

        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        for (day in 1..maxDays) {
            val tempCal = Calendar.getInstance()
            tempCal.set(year, month, day)
            val dateStr = dateFormatter.format(tempCal.time)

            list.add(
                CalendarDay(
                    dateString = dateStr,
                    dayNumber = day,
                    isToday = dateStr == todayString,
                    isSelected = dateStr == selectedDate,
                    count = records[dateStr] ?: 0
                )
            )
        }

        return list
    }
}

class CigaretteViewModelFactory(
    private val application: Application,
    private val repository: CigaretteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CigaretteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CigaretteViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

