package com.example.gerdachat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gerdachat.MainActivity
import com.example.gerdachat.R
import com.example.gerdachat.data.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object SpontaneousMessageManager {

    private const val CHANNEL_ID = "gerda_spontaneous_messages"
    private const val PREFS_NAME = "gerda_spontaneous_prefs"
    private const val KEY_DATE = "scheduled_date"
    private const val KEY_SLOTS = "scheduled_slots"

    fun init(context: Context, repository: ChatRepository) {
        createNotificationChannel(context)
        startPeriodicChecker(context.applicationContext, repository)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Gerda B. Berichten",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Meldingen voor spontane WhatsApp-berichten van Gerda"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startPeriodicChecker(context: Context, repository: ChatRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            // Initial delay
            delay(10_000)
            while (isActive) {
                try {
                    checkAndTriggerDue(context, repository)
                } catch (e: Exception) {
                    // Ignore transient errors
                }
                // Check every 45 seconds
                delay(45_000)
            }
        }
    }

    private suspend fun checkAndTriggerDue(context: Context, repository: ChatRepository) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        // Alleen tussen 9:00 en 20:00 (uur 9 t/m 19)
        if (hour < 9 || hour >= 20) return

        val currentMinute = hour * 60 + calendar.get(Calendar.MINUTE)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_DATE, "")

        val slots: MutableList<Pair<Int, Boolean>> = if (savedDate == todayStr) {
            val raw = prefs.getString(KEY_SLOTS, "") ?: ""
            parseSlots(raw)
        } else {
            // Generate 3 random slots between 9:00 (540 min) and 20:00 (1200 min)
            val s1 = Random.nextInt(555, 750)  // 09:15 - 12:30
            val s2 = Random.nextInt(765, 975)  // 12:45 - 16:15
            val s3 = Random.nextInt(990, 1185) // 16:30 - 19:45
            mutableListOf(Pair(s1, false), Pair(s2, false), Pair(s3, false))
        }

        var triggered = false
        for (i in slots.indices) {
            val (targetMin, sent) = slots[i]
            if (!sent && currentMinute >= targetMin) {
                slots[i] = Pair(targetMin, true)
                triggered = true
                break
            }
        }

        if (triggered) {
            prefs.edit()
                .putString(KEY_DATE, todayStr)
                .putString(KEY_SLOTS, serializeSlots(slots))
                .apply()

            val msg = repository.sendSpontaneousMessage()
            if (msg != null) {
                showNotification(context, msg.text.ifEmpty { "📷 Foto" })
            }
        } else if (savedDate != todayStr) {
            prefs.edit()
                .putString(KEY_DATE, todayStr)
                .putString(KEY_SLOTS, serializeSlots(slots))
                .apply()
        }
    }

    suspend fun triggerTestNow(context: Context, repository: ChatRepository) {
        val msg = repository.sendSpontaneousMessage()
        if (msg != null) {
            showNotification(context, msg.text.ifEmpty { "📷 Foto" })
        }
    }

    private fun showNotification(context: Context, bodyText: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Gerda B.")
                .setContentText(bodyText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Permission not granted yet
        } catch (e: Exception) {
            // Log fallback
        }
    }

    private fun parseSlots(raw: String): MutableList<Pair<Int, Boolean>> {
        val list = mutableListOf<Pair<Int, Boolean>>()
        if (raw.isBlank()) return list
        for (item in raw.split(";")) {
            val parts = item.split(":")
            if (parts.size == 2) {
                val min = parts[0].toIntOrNull() ?: continue
                val sent = parts[1] == "1"
                list.add(Pair(min, sent))
            }
        }
        return list
    }

    private fun serializeSlots(slots: List<Pair<Int, Boolean>>): String {
        return slots.joinToString(";") { "${it.first}:${if (it.second) "1" else "0"}" }
    }
}
