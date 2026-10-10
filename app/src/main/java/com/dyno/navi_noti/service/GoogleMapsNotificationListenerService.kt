package com.dyno.navi_noti.service

import android.app.Notification
import android.util.Log
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dyno.navi_noti.data.model.NavigationState
import com.dyno.navi_noti.data.repository.NavigationRepository
import com.dyno.navi_noti.service.icon.ManeuverIconReader
import com.dyno.navi_noti.service.parser.GoogleMapsNotificationParser

/// Dịch vụ hệ thống lắng nghe thông báo điều hướng từ Google Maps
class GoogleMapsNotificationListenerService : NotificationListenerService() {

    private lateinit var notificationManager: NavigationNotificationManager
    private lateinit var iconReader: ManeuverIconReader
    private var arrivalReceived = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = NavigationNotificationManager(applicationContext)
        iconReader = ManeuverIconReader(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        NavigationRepository.setServiceConnected(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NavigationRepository.setServiceConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn?.packageName == GOOGLE_MAPS_PACKAGE && Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Maps notification id=${sbn.id} category=${sbn.notification?.category} ongoing=${sbn.isOngoing}")
        }
        if (sbn == null || !sbn.isMapsNavigation()) return
        if (NavigationRepository.isSimulatorRunning.value) return // Ưu tiên chế độ mô phỏng nếu đang bật

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val additionalTexts = buildList {
            // Live Update (Android 16): chip status bar hiển thị shortCriticalText, thường là khoảng cách
            addNotNull(extras.getCharSequence(EXTRA_SHORT_CRITICAL_TEXT)?.toString())
            addNotNull(extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString())
            addNotNull(extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString())
            addNotNull(extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString())
            addNotNull(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString())
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.mapNotNull { it?.toString() }
                ?.let(::addAll)
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) logExtras(extras)

        val textStep = GoogleMapsNotificationParser.parse(title, text, subText, additionalTexts) ?: return
        val iconReading = iconReader.read(notification)
        val parsedStep = if (textStep.maneuver.isTurn) {
            iconReader.learn(iconReading, textStep.maneuver)
            textStep
        } else if (textStep.isDestination) {
            textStep
        } else {
            val iconManeuver = iconReader.resolve(iconReading)
            GoogleMapsNotificationParser.parse(title, text, subText, additionalTexts, iconManeuver) ?: textStep
        }

        if (arrivalReceived && !parsedStep.isDestination) return
        if (parsedStep.isDestination) arrivalReceived = true
        notificationManager.showOrUpdateNotification(parsedStep, iconReader.lastArrowBitmap)
        NavigationRepository.updateStep(parsedStep)
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap,
        reason: Int
    ) {
        super.onNotificationRemoved(sbn, rankingMap, reason)

        if (sbn.packageName == packageName &&
            sbn.id == NavigationNotificationManager.NOTIFICATION_ID &&
            isUserDismissal(reason)
        ) {
            val state = NavigationRepository.currentState.value
            val currentStep = NavigationRepository.currentStep.value
            if (state.shouldShowNotification && currentStep != null) {
                notificationManager.restoreNotification(currentStep)
            }
            return
        }

        // Gỡ tin cảnh báo của Maps (tắc đường, ngập...) không có nghĩa là kết thúc hành trình
        if (!sbn.isMapsNavigation()) return
        if (NavigationRepository.isSimulatorRunning.value) return

        arrivalReceived = false
        // Khi Google Maps gỡ thông báo, ta cũng lập tức gỡ thông báo trên smartwatch để tránh thông tin cũ
        notificationManager.dismissNotification()
        NavigationRepository.setState(NavigationState.COMPLETED)
    }

    companion object {
        const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
        private const val TAG = "NaviNoti"
        /// Notification.EXTRA_SHORT_CRITICAL_TEXT (API 36), dùng chuỗi để chạy được trên máy cũ
        private const val EXTRA_SHORT_CRITICAL_TEXT = "android.shortCriticalText"
    }

    /// Bật bằng `adb shell setprop log.tag.NaviNoti DEBUG` để xem cấu trúc notification Maps trên máy thật
    @Suppress("DEPRECATION")
    private fun logExtras(extras: android.os.Bundle) {
        val dump = extras.keySet().sorted().joinToString("\n") { key -> "  $key = ${extras.get(key)}" }
        Log.d(TAG, "Maps notification extras:\n$dump")
    }

    /// Maps còn đăng tin cảnh báo (tắc đường, ngập...) cùng package.
    /// Notification dẫn đường là ongoing (foreground service) và category navigation nếu có khai báo.
    private fun StatusBarNotification.isMapsNavigation(): Boolean {
        if (packageName != GOOGLE_MAPS_PACKAGE || !isOngoing) return false
        val category = notification?.category ?: return true
        return category == Notification.CATEGORY_NAVIGATION
    }

    private fun MutableList<String>.addNotNull(value: String?) {
        if (!value.isNullOrBlank()) add(value)
    }

    private fun isUserDismissal(reason: Int): Boolean =
        reason == REASON_CANCEL || reason == REASON_CANCEL_ALL
}
