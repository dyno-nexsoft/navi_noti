package com.dyno.navi_noti.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dyno.navi_noti.MainActivity
import com.dyno.navi_noti.R
import com.dyno.navi_noti.data.model.ManeuverType
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.data.preference.PreferencesManager
import com.dyno.navi_noti.util.withAppLanguage

/// Trình quản lý xuất bản và cập nhật thông báo điều hướng cho smartwatch
class NavigationNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val alertPolicy = NavigationAlertPolicy()

    init {
        createNotificationChannel()
    }

    /// Khởi tạo channel thông báo dành riêng cho smartwatch với độ ưu tiên cao
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /// Xuất bản hoặc cập nhật thông báo điều hướng duy nhất tới đồng hồ
    fun showOrUpdateNotification(step: NavigationStep) {
        if (!alertPolicy.shouldAlert(step)) return
        val notificationContext = context.withAppLanguage(
            PreferencesManager(context).appLanguage.value
        )

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_notification)
            .setContentTitle(formatNotificationTitle(step, notificationContext))
            .setContentText(step.formatContent(notificationContext))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(step.formatContent(notificationContext))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Trường hợp người dùng chưa cấp quyền POST_NOTIFICATIONS trên Android 13+
        }
    }

    private fun formatNotificationTitle(step: NavigationStep, context: Context): String {
        val arrow = when (step.maneuver) {
            ManeuverType.TURN_LEFT -> "←"
            ManeuverType.TURN_RIGHT -> "→"
            ManeuverType.STRAIGHT -> "↑"
            ManeuverType.ROUNDABOUT -> "↗"
            ManeuverType.DESTINATION -> "✓"
            ManeuverType.WAITING -> "…"
            ManeuverType.UNKNOWN -> "↑"
        }
        return "$arrow ${step.formatTitle(context)}"
    }

    /// Xóa bỏ hoàn toàn thông báo điều hướng khi hành trình tạm dừng hoặc kết thúc
    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        alertPolicy.reset()
    }

    companion object {
        const val CHANNEL_ID = "navi_noti_watch_channel"
        const val NOTIFICATION_ID = 1001
    }
}
