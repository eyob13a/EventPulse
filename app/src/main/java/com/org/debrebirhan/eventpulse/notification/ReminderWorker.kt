package com.org.debrebirhan.eventpulse.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val eventTitle = inputData.getString("eventTitle") ?: "Event Reminder"

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showNotification("Don't miss out!", "Your event '$eventTitle' is coming up soon!")

        return Result.success()
    }
}