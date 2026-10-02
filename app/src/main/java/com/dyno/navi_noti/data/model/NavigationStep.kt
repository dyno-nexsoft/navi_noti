package com.dyno.navi_noti.data.model

import android.content.Context
import com.dyno.navi_noti.R

/// Dữ liệu biểu diễn một bước điều hướng được tinh chỉnh để đọc tối ưu trên đồng hồ
data class NavigationStep(
    val action: String,
    val distanceText: String? = null,
    val streetName: String? = null,
    val distanceMeters: Int? = null,
    val maneuver: ManeuverType = ManeuverType.fromText(action),
    val isApproaching: Boolean = false,
    val isWaiting: Boolean = false,
    val isDestination: Boolean = false
) {
    /// Định dạng tiêu đề theo ngôn ngữ hệ thống hoặc ngữ cảnh ứng dụng
    fun formatTitle(context: Context): String = when {
        isDestination -> context.getString(R.string.action_arrived)
        isWaiting -> context.getString(R.string.action_waiting)
        maneuver == ManeuverType.TURN_LEFT -> context.getString(R.string.action_turn_left)
        maneuver == ManeuverType.TURN_RIGHT -> context.getString(R.string.action_turn_right)
        maneuver == ManeuverType.STRAIGHT -> context.getString(R.string.action_straight)
        maneuver == ManeuverType.ROUNDABOUT -> action
        else -> action
    }

    /// Tiêu đề đầy đủ dùng chung cho notification và phần xem trước trong ứng dụng
    fun formatNotificationTitle(context: Context): String {
        if (isWaiting || isDestination) return formatTitle(context)
        streetName?.takeIf(String::isNotBlank)?.let { return it }
        return maneuver.notificationArrow?.let { "$it ${formatTitle(context)}" } ?: formatTitle(context)
    }

    val formattedNotificationTitle: String
        get() = when {
            isWaiting -> "Đang chờ hướng dẫn"
            isDestination -> "Đã đến nơi"
            !streetName.isNullOrBlank() -> streetName
            maneuver.notificationArrow != null -> "${maneuver.notificationArrow} ${formattedTitle}"
            else -> formattedTitle
        }

    /// Định dạng nội dung thông báo đa ngôn ngữ ngắn gọn cho đồng hồ
    fun formatContent(context: Context): String {
        if (isWaiting) return context.getString(R.string.content_waiting)
        if (isDestination) return context.getString(R.string.content_arrived)

        val cleanStreet = streetName?.takeIf { it.isNotBlank() }

        return when {
            !distanceText.isNullOrBlank() &&
                maneuver != ManeuverType.UNKNOWN ->
                context.getString(R.string.content_remaining_action, distanceText, formatTitle(context))
            !distanceText.isNullOrBlank() &&
                cleanStreet != null -> context.getString(R.string.content_remaining, distanceText, cleanStreet)
            !distanceText.isNullOrBlank() -> context.getString(R.string.content_remaining_no_street, distanceText)
            isApproaching && cleanStreet != null -> context.getString(R.string.content_approaching, cleanStreet)
            isApproaching -> context.getString(R.string.content_approaching_no_street)
            cleanStreet != null -> cleanStreet
            else -> action
        }
    }

    /// Tiêu đề mặc định (phục vụ fallback hoặc unit test không cần context)
    val formattedTitle: String
        get() = when {
            isDestination -> "Đã đến nơi"
            isWaiting -> "Đang chờ hướng dẫn"
            else -> action
        }

    /// Nội dung mặc định (phục vụ fallback hoặc unit test không cần context)
    val formattedContent: String
        get() {
            if (isWaiting) return "Đang cập nhật lộ trình tiếp theo…"
            if (isDestination) {
                return streetName?.let { "Điểm đến: $it" } ?: "Bạn đã đến đích an toàn"
            }

            val prefix = when {
                !distanceText.isNullOrBlank() -> "Còn $distanceText"
                isApproaching -> "Đang đến điểm rẽ"
                else -> null
            }
            val cleanStreet = streetName?.takeIf { it.isNotBlank() }
            val displayAction = if (maneuver == ManeuverType.ROUNDABOUT) action else maneuver.defaultActionName

            return when {
                !distanceText.isNullOrBlank() && maneuver != ManeuverType.UNKNOWN ->
                    "Còn $distanceText · $displayAction"
                !distanceText.isNullOrBlank() && cleanStreet != null ->
                    "Còn $distanceText · $cleanStreet"
                !distanceText.isNullOrBlank() -> "Còn $distanceText"
                prefix != null && cleanStreet != null -> "$prefix · $cleanStreet"
                prefix != null -> prefix
                cleanStreet != null -> cleanStreet
                else -> action
            }
        }

    companion object {
        /// Tạo trạng thái chờ chỉ đường mới khi vừa rẽ xong
        fun waiting(): NavigationStep = NavigationStep(
            action = "Đang chờ hướng dẫn",
            isWaiting = true,
            maneuver = ManeuverType.WAITING
        )

        /// Tạo trạng thái đã đến đích
        fun arrived(destinationName: String? = null): NavigationStep = NavigationStep(
            action = "Đã đến nơi",
            streetName = destinationName,
            maneuver = ManeuverType.DESTINATION,
            isDestination = true
        )
    }
}
