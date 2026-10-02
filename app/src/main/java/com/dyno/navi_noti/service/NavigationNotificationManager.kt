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

/// Trình quản lý xuất bản và cập nhật thông báo điều hướng cho smartwatch
class NavigationNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    // Lưu vết trạng thái trước để xác định thời điểm cần rung báo động
    private var lastAction: String? = null
    private var lastStreet: String? = null
    private var hasAlerted300m = false
    private var hasAlerted100m = false
    private var hasAlertedTurnPoint = false

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
        val shouldAlert = evaluateAlertRequirement(step)

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
            .setContentTitle(step.formatTitle(context))
            .setContentText(step.formatContent(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(!shouldAlert)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Trường hợp người dùng chưa cấp quyền POST_NOTIFICATIONS trên Android 13+
        }
    }

    /// Xóa bỏ hoàn toàn thông báo điều hướng khi hành trình tạm dừng hoặc kết thúc
    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        resetAlertTriggers()
    }

    /// Đánh giá xem có nên rung cảnh báo trên smartwatch tại bước này hay không
    private fun evaluateAlertRequirement(step: NavigationStep): Boolean {
        val isNewStep = step.action != lastAction || step.streetName != lastStreet
        if (isNewStep) {
            lastAction = step.action
            lastStreet = step.streetName
            resetAlertTriggers()
            return true
        }

        if (step.isDestination) return true

        val distance = step.distanceMeters ?: return false

        // Mốc sắp đến chỗ rẽ (~300m)
        if (distance in 250..320 && !hasAlerted300m) {
            hasAlerted300m = true
            return true
        }

        // Mốc gần đến chỗ rẽ (~100m)
        if (distance in 70..130 && !hasAlerted100m) {
            hasAlerted100m = true
            return true
        }

        // Mốc ngay tại điểm rẽ
        if (step.isApproaching && !hasAlertedTurnPoint) {
            hasAlertedTurnPoint = true
            return true
        }

        return false
    }

    /// Thiết lập lại cờ báo động khi chuyển bước điều hướng mới
    private fun resetAlertTriggers() {
        hasAlerted300m = false
        hasAlerted100m = false
        hasAlertedTurnPoint = false
    }

    companion object {
        const val CHANNEL_ID = "navi_noti_watch_channel"
        const val NOTIFICATION_ID = 1001
    }
}
