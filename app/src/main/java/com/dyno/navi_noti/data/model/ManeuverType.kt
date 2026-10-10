package com.dyno.navi_noti.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dyno.navi_noti.R

/// Loại thao tác rẽ hoặc chuyển hướng trong lộ trình điều hướng
enum class ManeuverType(
    val defaultActionName: String,
    @get:DrawableRes val iconRes: Int,
    val notificationArrow: String?,
    @get:StringRes val titleRes: Int?
) {
    TURN_LEFT("Rẽ trái", R.drawable.ic_nav_turn_left, "←", R.string.action_turn_left),
    TURN_RIGHT("Rẽ phải", R.drawable.ic_nav_turn_right, "→", R.string.action_turn_right),
    SLIGHT_LEFT("Chếch trái", R.drawable.ic_nav_turn_left, "↖", R.string.action_slight_left),
    SLIGHT_RIGHT("Chếch phải", R.drawable.ic_nav_turn_right, "↗", R.string.action_slight_right),
    SHARP_LEFT("Rẽ gắt trái", R.drawable.ic_nav_turn_left, "↙", R.string.action_sharp_left),
    SHARP_RIGHT("Rẽ gắt phải", R.drawable.ic_nav_turn_right, "↘", R.string.action_sharp_right),
    KEEP_LEFT("Đi bên trái", R.drawable.ic_nav_turn_left, "↖", R.string.action_keep_left),
    KEEP_RIGHT("Đi bên phải", R.drawable.ic_nav_turn_right, "↗", R.string.action_keep_right),
    U_TURN("Quay đầu", R.drawable.ic_nav_turn_left, "↶", R.string.action_u_turn),
    STRAIGHT("Đi thẳng", R.drawable.ic_nav_straight, "↑", R.string.action_straight),
    ROUNDABOUT("Đi theo lối ra", R.drawable.ic_nav_roundabout, "↻", R.string.action_roundabout),
    DESTINATION("Điểm đến", R.drawable.ic_nav_destination, "◎", R.string.action_destination),
    WAITING("Đang chờ hướng dẫn", R.drawable.ic_nav_waiting, "…", R.string.action_waiting),
    UNKNOWN("Tiếp tục theo lộ trình", R.drawable.ic_nav_notification, null, null);

    /// Thao tác có hướng rõ ràng (rẽ/quay đầu/vòng xoay), khác với đi thẳng hoặc không xác định
    val isTurn: Boolean
        get() = this != STRAIGHT && this != DESTINATION && this != WAITING && this != UNKNOWN

    companion object {
        /// Phân tích chuỗi hành động để xác định loại maneuver phù hợp.
        /// Thứ tự kiểm tra đi từ cụ thể đến chung để "chếch trái" không bị nhận thành "rẽ trái".
        fun fromText(text: String): ManeuverType {
            val lower = text.lowercase()
            fun has(vararg keys: String) = keys.any(lower::contains)
            return when {
                has("đến nơi", "đích", "arrived", "destination") -> DESTINATION
                has("quay đầu", "quay lại", "u-turn", "u turn", "make a u") -> U_TURN
                has("vòng xuyến", "vòng xoay", "lối ra", "roundabout", "exit") -> ROUNDABOUT
                has("chếch sang trái", "chếch trái", "rẽ nhẹ sang trái", "slight left", "bear left") -> SLIGHT_LEFT
                has("chếch sang phải", "chếch phải", "rẽ nhẹ sang phải", "slight right", "bear right") -> SLIGHT_RIGHT
                has("gắt sang trái", "gắt trái", "rẽ ngoặt trái", "sharp left") -> SHARP_LEFT
                has("gắt sang phải", "gắt phải", "rẽ ngoặt phải", "sharp right") -> SHARP_RIGHT
                has("giữ bên trái", "đi bên trái", "đi về bên trái", "giữ làn trái", "keep left") -> KEEP_LEFT
                has("giữ bên phải", "đi bên phải", "đi về bên phải", "giữ làn phải", "keep right") -> KEEP_RIGHT
                has("trái", "left") -> TURN_LEFT
                has("phải", "right") -> TURN_RIGHT
                has("thẳng", "straight", "tiếp tục", "continue", "đi tiếp", "head ") -> STRAIGHT
                has("chờ", "waiting") -> WAITING
                else -> UNKNOWN
            }
        }
    }
}
