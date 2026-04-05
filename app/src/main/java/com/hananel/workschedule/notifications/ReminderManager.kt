package com.hananel.workschedule.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hananel.workschedule.data.Reminder
import com.hananel.workschedule.data.ReminderTime
import java.util.Calendar

object ReminderManager {

    private const val PREFS_NAME = "reminders_prefs"
    private const val KEY_REMINDERS = "reminders_list"
    private val gson = Gson()

    // ─── Persistence ────────────────────────────────────────────────────────

    fun getReminders(context: Context): List<Reminder> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_REMINDERS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Reminder>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveReminders(context: Context, reminders: List<Reminder>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_REMINDERS, gson.toJson(reminders)).apply()
    }

    // ─── Scheduling ─────────────────────────────────────────────────────────

    fun scheduleAll(context: Context) {
        getReminders(context).filter { it.isEnabled }.forEach { scheduleReminder(context, it) }
    }

    fun scheduleReminder(context: Context, reminder: Reminder) {
        if (!reminder.isEnabled) return
        reminder.times.forEachIndexed { index, time ->
            scheduleAlarm(context, reminder.id, reminder.dayOfWeek, time, index, reminder.isRecurring)
        }
    }

    fun scheduleAlarm(
        context: Context,
        reminderId: String,
        dayOfWeek: Int,
        time: ReminderTime,
        timeIndex: Int,
        isRecurring: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("time_index", timeIndex)
            putExtra("is_recurring", isRecurring)
        }
        val requestCode = alarmRequestCode(reminderId, timeIndex)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = nextAlarmTime(dayOfWeek, time.hour, time.minute)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        reminder.times.forEachIndexed { index, _ ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val requestCode = alarmRequestCode(reminder.id, index)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun cancelAll(context: Context) {
        getReminders(context).forEach { cancelReminder(context, it) }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    fun nextAlarmTime(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (alarm.timeInMillis <= now.timeInMillis) {
            alarm.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return alarm.timeInMillis
    }

    fun alarmRequestCode(reminderId: String, timeIndex: Int): Int =
        (reminderId.hashCode() and 0x0FFF) * 100 + timeIndex
}
