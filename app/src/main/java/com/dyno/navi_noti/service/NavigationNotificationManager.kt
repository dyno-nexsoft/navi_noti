package com.dyno.navi_noti.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dyno.navi_noti.MainActivity
import com.dyno.navi_noti.R
import com.dyno.navi_noti.data.model.NavigationStep
import com.dyno.navi_noti.data.model.NotificationFormatter
import com.dyno.navi_noti.data.preference.PreferencesManager
import com.dyno.navi_noti.data.repository.NavigationRepository
import com.dyno.navi_noti.util.withAppLanguage

/// Trình quản lý xuất bản và cập nhật thông báo điều hướng cho smartwatch
class NavigationNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)
    // Đọc cấu hình mỗi lần quyết định để thay đổi trong Cài đặt có hiệu lực ngay giữa hành trình
    private val alertPolicy = NavigationAlertPolicy(settings = { PreferencesManager(context).alertSettings })
    private var lastPostedStep: NavigationStep? = null
    private var lastArrowIcon: Bitmap? = null

    init {
        createNotificationChannel()
        current = this
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
    /// [arrowIcon] là mũi tên gốc của Google Maps, chuyển tiếp để đồng hồ hiển thị đúng hướng rẽ
    fun showOrUpdateNotification(step: NavigationStep, arrowIcon: Bitmap? = null) {
        // Policy vẫn chạy khi im lặng để các mốc đã qua không rung bù lúc bật lại
        val shouldAlert = alertPolicy.shouldAlert(step) && !NavigationRepository.isTripMuted.value
        if (shouldAlert && Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "VIBRATE ${step.maneuver} ${step.distanceText.orEmpty()} ${step.streetName.orEmpty()}")
        }
        if (!shouldAlert && step == lastPostedStep && arrowIcon?.sameAs(lastArrowIcon) != false) return
        lastArrowIcon = arrowIcon
        postNotification(step, silent = !shouldAlert)
    }

    /// Khôi phục notification đã bị người dùng xóa khi hành trình vẫn đang diễn ra
    fun restoreNotification(step: NavigationStep) {
        postNotification(step, silent = true)
    }

    private fun postNotification(step: NavigationStep, silent: Boolean) {
        val preferences = PreferencesManager(context)
        val notificationContext = context.withAppLanguage(preferences.appLanguage.value)
        val text = NotificationFormatter.format(
            step,
            notificationContext,
            preferences.notificationStyle.value,
            preferences.notificationTemplate.value
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
            .setSmallIcon(step.maneuver.iconRes)
            .setLargeIcon(lastArrowIcon)
            .setContentTitle(text.title)
            .setContentText(text.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text.content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setSubText(step.eta?.takeIf { preferences.showEta.value })
            .addAction(muteAction(notificationContext))
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

    private fun muteAction(localizedContext: Context): NotificationCompat.Action {
        val intent = Intent(context, MuteTripReceiver::class.java)
            .setAction(MuteTripReceiver.ACTION_TOGGLE_MUTE)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val muted = NavigationRepository.isTripMuted.value
        return NotificationCompat.Action.Builder(
            if (muted) R.drawable.ic_nav_notification else R.drawable.ic_nav_waiting,
            localizedContext.getString(if (muted) R.string.action_unmute_trip else R.string.action_mute_trip),
            pendingIntent
        ).build()
    }

    /// Xóa bỏ hoàn toàn thông báo điều hướng khi hành trình tạm dừng hoặc kết thúc
    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        alertPolicy.reset()
        lastPostedStep = null
        lastArrowIcon = null
    }

    companion object {
        const val CHANNEL_ID = "navi_noti_watch_channel"
        const val NOTIFICATION_ID = 1001

        /// Instance đang đăng notification, để nút trên notification vẽ lại ngay
        @Volatile
        var current: NavigationNotificationManager? = null
            private set
        private const val TAG = "NaviNoti"
    }
}
