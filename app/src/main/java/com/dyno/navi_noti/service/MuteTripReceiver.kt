package com.dyno.navi_noti.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dyno.navi_noti.data.repository.NavigationRepository

/// Nhận nút "Im lặng chuyến này" / "Bật rung lại" trên notification
class MuteTripReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val muted = !NavigationRepository.isTripMuted.value
        NavigationRepository.setTripMuted(muted)
        // Vẽ lại notification ngay để nút đổi nhãn, không chờ Maps cập nhật
        NavigationRepository.currentStep.value?.let {
            NavigationNotificationManager.current?.restoreNotification(it)
        }
    }

    companion object {
        const val ACTION_TOGGLE_MUTE = "com.dyno.navi_noti.action.TOGGLE_MUTE"
    }
}
