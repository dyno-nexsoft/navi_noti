package com.dyno.navi_noti.data.model

import androidx.annotation.DrawableRes
import com.dyno.navi_noti.R

/// Loại thao tác rẽ hoặc chuyển hướng trong lộ trình điều hướng
enum class ManeuverType(
    val defaultActionName: String,
    @get:DrawableRes val iconRes: Int,
    val notificationArrow: String
) {
    TURN_LEFT("Rẽ trái", R.drawable.ic_nav_turn_left, "←"),
    TURN_RIGHT("Rẽ phải", R.drawable.ic_nav_turn_right, "→"),
    STRAIGHT("Đi thẳng", R.drawable.ic_nav_straight, "↑"),
    ROUNDABOUT("Đi theo lối ra", R.drawable.ic_nav_roundabout, "↗"),
    DESTINATION("Đã đến nơi", R.drawable.ic_nav_destination, "✓"),
    WAITING("Đang chờ hướng dẫn", R.drawable.ic_nav_waiting, "…"),
    UNKNOWN("Tiếp tục theo lộ trình", R.drawable.ic_nav_notification, "↑");

    companion object {
        /// Phân tích chuỗi hành động để xác định loại maneuver phù hợp
        fun fromText(text: String): ManeuverType {
            val lower = text.lowercase()
            return when {
                lower.contains("đến nơi") || lower.contains("đích") || lower.contains("arrived") || lower.contains("destination") -> DESTINATION
                lower.contains("trái") || lower.contains("left") -> TURN_LEFT
                lower.contains("phải") || lower.contains("right") -> TURN_RIGHT
                lower.contains("thẳng") || lower.contains("straight") || lower.contains("tiếp tục") || lower.contains("continue") -> STRAIGHT
                lower.contains("vòng xuyến") || lower.contains("vòng xoay") || lower.contains("lối ra") || lower.contains("roundabout") || lower.contains("exit") -> ROUNDABOUT
                lower.contains("chờ") || lower.contains("waiting") -> WAITING
                else -> UNKNOWN
            }
        }
    }
}
