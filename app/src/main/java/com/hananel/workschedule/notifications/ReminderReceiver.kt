package com.hananel.workschedule.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hananel.workschedule.MainActivity
import com.hananel.workschedule.R
import com.hananel.workschedule.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "work_schedule_reminders"
        const val NOTIFICATION_ID = 2001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val timeIndex = intent.getIntExtra("time_index", 0)
        val isRecurring = intent.getBooleanExtra("is_recurring", true)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val weekStarts = db.scheduleDao().getAllSchedulesList().map { it.weekStart }
                if (!hasScheduleForNextWeek(weekStarts)) {
                    showNotification(context)
                }

                // Reschedule for next week if recurring
                if (isRecurring) {
                    val reminder = ReminderManager.getReminders(context).firstOrNull { it.id == reminderId }
                    val time = reminder?.times?.getOrNull(timeIndex)
                    if (reminder != null && reminder.isEnabled && time != null) {
                        val nextWeek = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_WEEK, reminder.dayOfWeek)
                            set(Calendar.HOUR_OF_DAY, time.hour)
                            set(Calendar.MINUTE, time.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            add(Calendar.WEEK_OF_YEAR, 1)
                        }
                        val newIntent = Intent(context, ReminderReceiver::class.java).apply {
                            putExtra("reminder_id", reminderId)
                            putExtra("time_index", timeIndex)
                            putExtra("is_recurring", true)
                        }
                        val requestCode = ReminderManager.alarmRequestCode(reminderId, timeIndex)
                        val pendingIntent = PendingIntent.getBroadcast(
                            context, requestCode, newIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
                                as android.app.AlarmManager
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                nextWeek.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.set(
                                android.app.AlarmManager.RTC_WAKEUP,
                                nextWeek.timeInMillis,
                                pendingIntent
                            )
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun hasScheduleForNextWeek(weekStarts: List<String>): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val nextSunday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val twoWeeksOut = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 14) }

        return weekStarts.any { dateStr ->
            if (dateStr == "__TEMP_DRAFT__") return@any false
            try {
                val date = sdf.parse(dateStr) ?: return@any false
                val cal = Calendar.getInstance().apply { time = date }
                // Check if weekStart falls between next Sunday and 2 weeks from today
                cal.timeInMillis >= nextSunday.timeInMillis && cal.timeInMillis <= twoWeeksOut.timeInMillis
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun showNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "תזכורות סידור עבודה",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "תזכורות לעריכת סידור העבודה השבועי"
            }
            nm.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_logo_new)
            .setContentTitle("זמן לסידור עבודה!")
            .setContentText("לא נמצא סידור לשבוע הבא. לחץ כדי ליצור.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("לא קיים סידור עבודה לשבוע הבא. הגיע הזמן ליצור אחד!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }
}
