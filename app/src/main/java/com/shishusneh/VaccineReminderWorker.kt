package com.shishusneh

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shishusneh.data.ShishuDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

class VaccineReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = ShishuDatabase.getDatabase(applicationContext)
        val dao = database.dao()
        
        val profile = dao.getBabyProfile().firstOrNull() ?: return Result.success()
        val babyName = profile.name
        val vaccines = dao.getAllVaccines().firstOrNull() ?: return Result.success()
        
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val todayDate = now.get(Calendar.DAY_OF_YEAR)

        // Target hours: 9:00 AM (9), 12:00 AM (0), 3:00 AM (3), 6:00 AM (6)
        val targetHours = listOf(12, 13, 17, 21)
        
        if (currentHour !in targetHours) return Result.success()

        // SharedPrefs to avoid double notifications in the same hour slot
        val prefs = applicationContext.getSharedPreferences("vaccine_reminders", Context.MODE_PRIVATE)
        val lastNotifiedSlot = prefs.getString("last_notified_slot", "")
        val currentSlot = "$todayDate-$currentHour"
        
        if (lastNotifiedSlot == currentSlot) return Result.success()

        val oneWeekMillis = 7L * 24 * 60 * 60 * 1000L
        val currentTimeMillis = now.timeInMillis
        
        var notifiedAny = false
        vaccines.forEach { vaccine ->
            if (!vaccine.isCompleted && vaccine.dueDateMillis > 0) {
                val timeUntilDue = vaccine.dueDateMillis - currentTimeMillis
                
                // Logic: Notify if we are within the 1-week window before the due date
                if (timeUntilDue in 0..oneWeekMillis) {
                    showNotification(
                        vaccine.diseaseName,
                        "Mandatory Vaccination Reminder for $babyName: ${vaccine.diseaseName} is due before ${formatDate(vaccine.dueDateMillis)}. Please visit the health center today."
                    )
                    notifiedAny = true
                }
            }
        }
        
        if (notifiedAny) {
            prefs.edit().putString("last_notified_slot", currentSlot).apply()
        }
        
        return Result.success()
    }

    private fun formatDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun showNotification(title: String, content: String) {
        val channelId = "vaccine_reminders"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Vaccine Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Shishu-Sneh Alert")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .build()

        // Use a unique ID based on vaccine name to allow multiple notifications if multiple are due
        notificationManager.notify(title.hashCode(), notification)
    }
}