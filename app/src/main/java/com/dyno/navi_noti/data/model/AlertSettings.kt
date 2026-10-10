package com.dyno.navi_noti.data.model

import androidx.annotation.StringRes
import com.dyno.navi_noti.R

/// Những thời điểm đồng hồ được phép rung; các cập nhật khác luôn im lặng
data class AlertSettings(
    /// Rung khi còn ~100 m tới chỗ rẽ
    val alertFar: Boolean = true,
    /// Rung khi còn ~30 m tới chỗ rẽ
    val alertNear: Boolean = true,
    /// Rung khi vừa tới chỗ rẽ (Maps chuyển sang bước kế tiếp)
    val alertTurnPoint: Boolean = false,
    /// Rung khi đến nơi
    val alertArrival: Boolean = true,
    /// Chỉ rung cho bước có rẽ; bước đi thẳng chỉ cập nhật im lặng
    val turnsOnly: Boolean = true,
    /// Khoảng nghỉ tối thiểu giữa hai lần rung (trừ lúc đến nơi), tránh rung dồn khi các chỗ rẽ sát nhau
    val cooldownSeconds: Int = 8
) {
    /// Số lần rung tối đa cho một chỗ rẽ, để người dùng hình dung mức độ
    val maxAlertsPerTurn: Int
        get() = listOf(alertFar, alertNear, alertTurnPoint).count { it }

    companion object {
        val COOLDOWN_OPTIONS = listOf(0, 5, 8, 15, 30)
    }
}

/// Mức rung soạn sẵn; [CUSTOM] dùng cấu hình người dùng tự chọn
enum class AlertPreset(
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int,
    val settings: AlertSettings?
) {
    QUIET(
        R.string.alert_quiet, R.string.alert_quiet_desc,
        AlertSettings(alertFar = false, alertNear = true, alertTurnPoint = false, cooldownSeconds = 15)
    ),
    BALANCED(R.string.alert_balanced, R.string.alert_balanced_desc, AlertSettings()),
    FULL(
        R.string.alert_full, R.string.alert_full_desc,
        AlertSettings(alertTurnPoint = true, turnsOnly = false, cooldownSeconds = 0)
    ),
    CUSTOM(R.string.alert_custom, R.string.alert_custom_desc, null);

    companion object {
        fun fromName(name: String?): AlertPreset = entries.firstOrNull { it.name == name } ?: BALANCED
    }
}
