package com.dyno.navi_noti.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dyno.navi_noti.data.model.NavigationState
import com.dyno.navi_noti.data.repository.NavigationRepository
import com.dyno.navi_noti.service.parser.GoogleMapsNotificationParser

/// Dịch vụ hệ thống lắng nghe thông báo điều hướng từ Google Maps
class GoogleMapsNotificationListenerService : NotificationListenerService() {

    private lateinit var notificationManager: NavigationNotificationManager
    private var arrivalReceived = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = NavigationNotificationManager(applicationContext)
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
        if (sbn == null || sbn.packageName != GOOGLE_MAPS_PACKAGE) return
        if (NavigationRepository.isSimulatorRunning.value) return // Ưu tiên chế độ mô phỏng nếu đang bật

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val parsedStep = GoogleMapsNotificationParser.parse(title, text, subText)
        if (parsedStep != null) {
            if (arrivalReceived && !parsedStep.isDestination) return
            if (parsedStep.isDestination) arrivalReceived = true
            notificationManager.showOrUpdateNotification(parsedStep)
            NavigationRepository.updateStep(parsedStep)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || sbn.packageName != GOOGLE_MAPS_PACKAGE) return
        if (NavigationRepository.isSimulatorRunning.value) return

        arrivalReceived = false
        // Khi Google Maps gỡ thông báo, ta cũng lập tức gỡ thông báo trên smartwatch để tránh thông tin cũ
        notificationManager.dismissNotification()
        NavigationRepository.setState(NavigationState.COMPLETED)
    }

    companion object {
        const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }
}
