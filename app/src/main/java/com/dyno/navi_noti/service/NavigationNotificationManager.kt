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
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.data.preference.PreferencesManager
import com.dyno.navi_noti.util.withAppLanguage

/// Trình quản lý xuất bản và cập nhật thông báo điều hướng cho smartwatch
class NavigationNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val alertPolicy = NavigationAlertPolicy()
    private var lastPostedStep: NavigationStep? = null

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
        val shouldAlert = alertPolicy.shouldAlert(step)
        val oldStreet = lastPostedStep?.streetName?.takeIf(String::isNotBlank)
        val newStreet = step.streetName?.takeIf(String::isNotBlank)
        val streetChanged = lastPostedStep != null &&
            oldStreet != newStreet &&
            (oldStreet != null || newStreet != null)
        if (!shouldAlert && !streetChanged) return
        postNotification(step, silent = !shouldAlert)
    }

    /// Khôi phục notification đã bị người dùng xóa khi hành trình vẫn đang diễn ra
    fun restoreNotification(step: NavigationStep) {
        postNotification(step, silent = true)
    }

    private fun postNotification(step: NavigationStep, silent: Boolean) {
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
            .setContentTitle(step.formatNotificationTitle(notificationContext))
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
            .setOnlyAlertOnce(true)
            .setSilent(silent)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
            lastPostedStep = step
        } catch (_: SecurityException) {
            // Trường hợp người dùng chưa cấp quyền POST_NOTIFICATIONS trên Android 13+
        }
    }

    /// Xóa bỏ hoàn toàn thông báo điều hướng khi hành trình tạm dừng hoặc kết thúc
    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        alertPolicy.reset()
        lastPostedStep = null
    }

    companion object {
        const val CHANNEL_ID = "navi_noti_watch_channel"
        const val NOTIFICATION_ID = 1001
    }
}
