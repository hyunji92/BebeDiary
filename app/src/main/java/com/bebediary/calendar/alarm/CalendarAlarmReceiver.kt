package com.bebediary.calendar.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bebediary.MainActivity
import com.bebediary.R
import com.bebediary.util.Constants

class CalendarAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        deliverNotification(context, intent)
    }

    private fun deliverNotification(context: Context, intent: Intent) {
        Log.d("CalendarAlarmReceiver", "deliverNotification")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알림 메시지
        val isBefore = intent.getBooleanExtra("isBefore", false)
        val content = intent.getStringExtra("content") ?: "일정"

        // 알림 메시지 설정
        val contentText = if (isBefore)
            "$content 하루전 입니다"
        else
            "$content 당일입니다"

        // Pending Intent
        val pendingIntent = PendingIntent.getActivity(
            context,
            Constants.notificationCalendarRequestCode,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, Constants.notificationChannelIdCalendar)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("일정 알림")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        notificationManager.notify(Constants.notificationCalendarRequestCode, builder.build())
    }

}